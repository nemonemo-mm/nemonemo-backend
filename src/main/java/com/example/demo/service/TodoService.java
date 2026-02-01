package com.example.demo.service;

import com.example.demo.domain.entity.*;
import com.example.demo.domain.enums.TodoStatus;
import com.example.demo.dto.todo.TodoAssigneeDto;
import com.example.demo.dto.todo.TodoCreateRequest;
import com.example.demo.dto.todo.TodoResponse;
import com.example.demo.dto.todo.TodoResponseDto;
import com.example.demo.dto.todo.TodoStatusUpdateRequest;
import com.example.demo.dto.todo.TodoUpdateRequest;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PositionRepository positionRepository;
    private final TodoAttendeeRepository todoAttendeeRepository;
    private final TodoPositionRepository todoPositionRepository;
    private final UserRepository userRepository;
    private final ExpoNotificationService expoNotificationService;
    private final DeviceTokenService deviceTokenService;
    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional
    public TodoResponseDto createTodo(Long userId, TodoCreateRequest request) {
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        TeamMember creatorMember = teamMemberRepository.findByTeamIdAndUserId(team.getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("팀원이 아닌 사용자는 투두를 생성할 수 없습니다."));

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Todo todo = Todo.builder()
                .team(team)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TodoStatus.TODO)
                .endAt(request.getEndAt())
                .place(request.getPlace())
                .url(request.getUrl())
                .createdBy(creator)
                .build();

        todo = todoRepository.save(todo);

        // 담당자 설정 (없으면 생성자 기본 담당자)
        List<Long> assigneeMemberIds = request.getAssigneeMemberIds();
        List<TodoAttendee> assignees = new ArrayList<>();
        if (assigneeMemberIds == null || assigneeMemberIds.isEmpty()) {
            assignees.add(buildTodoAttendee(todo, creatorMember));
        } else {
            List<TeamMember> members = teamMemberRepository.findAllById(assigneeMemberIds);
            for (TeamMember member : members) {
                assignees.add(buildTodoAttendee(todo, member));
            }
        }
        todoAttendeeRepository.saveAll(assignees);

        // 포지션 설정
        if (request.getPositionIds() != null && !request.getPositionIds().isEmpty()) {
            List<Position> positions = positionRepository.findAllById(request.getPositionIds());
            List<TodoPosition> todoPositions = new ArrayList<>();
            for (int i = 0; i < positions.size(); i++) {
                Position position = positions.get(i);
                TodoPosition tp = TodoPosition.builder()
                        .id(new TodoPositionId(todo.getId(), position.getId()))
                        .todo(todo)
                        .position(position)
                        .orderIndex(i)
                        .build();
                todoPositions.add(tp);
            }
            todoPositionRepository.saveAll(todoPositions);
        }

        // 투두 생성 알림 전송 (생성자 제외)
        sendTodoChangeNotification(todo, userId);

        return toResponse(todo);
    }

    @Transactional
    public TodoResponseDto updateTodo(Long userId, Long todoId, TodoUpdateRequest request) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("투두를 찾을 수 없습니다."));

        if (!teamMemberRepository.existsByTeamIdAndUserId(todo.getTeam().getId(), userId)) {
            throw new IllegalArgumentException("팀원이 아닌 사용자는 투두를 수정할 수 없습니다.");
        }

        if (request.getTitle() != null) todo.setTitle(request.getTitle());
        if (request.getDescription() != null) todo.setDescription(request.getDescription());
        if (request.getStatus() != null) todo.setStatus(request.getStatus());
        if (request.getEndAt() != null) todo.setEndAt(request.getEndAt());
        if (request.getPlace() != null) todo.setPlace(request.getPlace());
        if (request.getUrl() != null) todo.setUrl(request.getUrl());

        if (request.getAssigneeMemberIds() != null) {
            todo.getAssignees().clear();
            List<TeamMember> members = teamMemberRepository.findAllById(request.getAssigneeMemberIds());
            for (TeamMember member : members) {
                todo.getAssignees().add(buildTodoAttendee(todo, member));
            }
        }

        if (request.getPositionIds() != null) {
            todo.getPositions().clear();
            List<Position> positions = positionRepository.findAllById(request.getPositionIds());
            int index = 0;
            for (Position position : positions) {
                TodoPosition tp = TodoPosition.builder()
                        .id(new TodoPositionId(todo.getId(), position.getId()))
                        .todo(todo)
                        .position(position)
                        .orderIndex(index++)
                        .build();
                todo.getPositions().add(tp);
            }
        }

        // 지연 로딩된 연관 관계 초기화 (LazyInitializationException 방지)
        todo.getTeam().getName(); // team 초기화
        todo.getCreatedBy().getName(); // createdBy 초기화
        if (todo.getAssignees() != null) {
            todo.getAssignees().forEach(a -> {
                a.getMember().getUser().getName(); // assignee -> member -> user 초기화
            });
        }
        if (todo.getPositions() != null) {
            todo.getPositions().forEach(tp -> {
                tp.getPosition().getId(); // position 초기화
                tp.getPosition().getColorHex(); // position 초기화
            });
        }

        // 투두 수정 알림 전송 (수정자 제외)
        sendTodoChangeNotification(todo, userId);

        return toResponse(todo);
    }

    /**
     * 투두 완료 여부만 수정
     */
    @Transactional
    public TodoResponseDto updateTodoStatus(Long userId, Long todoId, TodoStatusUpdateRequest request) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("투두를 찾을 수 없습니다."));

        if (!teamMemberRepository.existsByTeamIdAndUserId(todo.getTeam().getId(), userId)) {
            throw new IllegalArgumentException("팀원이 아닌 사용자는 투두를 수정할 수 없습니다.");
        }

        todo.setStatus(request.getStatus());
        todo = todoRepository.save(todo);

        // 지연 로딩된 연관 관계 초기화 (LazyInitializationException 방지)
        todo.getTeam().getName(); // team 초기화
        todo.getCreatedBy().getName(); // createdBy 초기화
        if (todo.getAssignees() != null) {
            todo.getAssignees().forEach(a -> {
                a.getMember().getUser().getName(); // assignee -> member -> user 초기화
            });
        }
        if (todo.getPositions() != null) {
            todo.getPositions().forEach(tp -> {
                tp.getPosition().getId(); // position 초기화
                tp.getPosition().getColorHex(); // position 초기화
            });
        }

        // 투두 상태 변경 알림 전송 (수정자 제외)
        sendTodoChangeNotification(todo, userId);

        return toResponse(todo);
    }

    @Transactional
    public void deleteTodo(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("투두를 찾을 수 없습니다."));

        if (!teamMemberRepository.existsByTeamIdAndUserId(todo.getTeam().getId(), userId)) {
            throw new IllegalArgumentException("팀원이 아닌 사용자는 투두를 삭제할 수 없습니다.");
        }

        todoRepository.delete(todo);
    }

    @Transactional(readOnly = true)
    public List<TodoResponseDto> getTeamTodos(Long userId, Long teamId, LocalDateTime start, LocalDateTime end) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new IllegalArgumentException("팀원이 아닌 사용자는 팀 투두를 조회할 수 없습니다.");
        }
        List<TodoResponse> responses = todoRepository.findByTeamAndRange(teamId, start, end);
        return enrichTodoResponses(responses);
    }

    @Transactional(readOnly = true)
    public List<TodoResponseDto> getMyTodos(Long userId, LocalDateTime start, LocalDateTime end) {
        List<TeamMember> members = teamMemberRepository.findByUserId(userId);
        List<Long> memberIds = members.stream().map(TeamMember::getId).toList();
        if (memberIds.isEmpty()) {
            return List.of();
        }
        List<TodoResponse> responses = todoRepository.findByAssigneesAndRange(memberIds, start, end);
        return enrichTodoResponses(responses);
    }

    private TodoAttendee buildTodoAttendee(Todo todo, TeamMember member) {
        TodoAttendeeId id = new TodoAttendeeId(todo.getId(), member.getId());
        return TodoAttendee.builder()
                .id(id)
                .todo(todo)
                .member(member)
                .build();
    }

    private List<TodoResponseDto> enrichTodoResponses(List<TodoResponse> responses) {
        return responses.stream()
                .map(response -> {
                    // positionIds 및 대표 포지션 컬러 가져오기
                    List<Long> positionIds = todoRepository.findPositionIdsByTodoId(response.getId());
                    String representativeColorHex = null;
                    if (!positionIds.isEmpty()) {
                        representativeColorHex = positionRepository.findById(positionIds.get(0))
                                .map(Position::getColorHex)
                                .orElse(null);
                    }
                    
                    // assignees 가져오기
                    List<Object[]> assigneeInfoList = todoRepository.findAssigneeInfoByTodoId(response.getId());
                    List<TodoAssigneeDto> assignees = assigneeInfoList.stream()
                            .map(info -> TodoAssigneeDto.builder()
                                    .memberId((Long) info[0])
                                    .userName((String) info[1])
                                    .build())
                            .collect(Collectors.toList());
                    
                    Long primaryAssigneeId = assignees.isEmpty() ? null : assignees.get(0).getMemberId();
                    String primaryAssigneeName = assignees.isEmpty() ? null : assignees.get(0).getUserName();
                    
                    return TodoResponseDto.builder()
                            .id(response.getId())
                            .teamId(response.getTeamId())
                            .teamName(response.getTeamName())
                            .title(response.getTitle())
                            .description(response.getDescription())
                            .status(response.getStatus())
                            .endAt(response.getEndAt())
                            .place(response.getPlace())
                            .url(response.getUrl())
                            .createdById(response.getCreatedById())
                            .createdByName(response.getCreatedByName())
                            .assigneeMemberId(primaryAssigneeId)
                            .assigneeMemberUserName(primaryAssigneeName)
                            .assignees(assignees)
                            .positionIds(positionIds)
                            .representativeColorHex(representativeColorHex)
                            .createdAt(response.getCreatedAt())
                            .updatedAt(response.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private TodoResponseDto toResponse(Todo todo) {
        List<TodoPosition> sortedPositions = todo.getPositions() == null
                ? List.of()
                : todo.getPositions().stream()
                .sorted(Comparator.comparing(TodoPosition::getOrderIndex))
                .toList();

        List<Long> positionIds = sortedPositions.stream()
                .map(tp -> tp.getPosition().getId())
                .toList();

        List<TodoAssigneeDto> assignees = todo.getAssignees() == null
                ? List.of()
                : todo.getAssignees().stream()
                .map(a -> TodoAssigneeDto.builder()
                        .memberId(a.getMember().getId())
                        .userName(a.getMember().getUser().getName())
                        .build())
                .toList();

        Long primaryAssigneeId = assignees.isEmpty() ? null : assignees.get(0).getMemberId();
        String primaryAssigneeName = assignees.isEmpty() ? null : assignees.get(0).getUserName();

        String representativeColorHex = sortedPositions.isEmpty()
                ? null
                : sortedPositions.get(0).getPosition().getColorHex();

        return TodoResponseDto.builder()
                .id(todo.getId())
                .teamId(todo.getTeam().getId())
                .teamName(todo.getTeam().getName())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .status(todo.getStatus())
                .endAt(todo.getEndAt())
                .place(todo.getPlace())
                .url(todo.getUrl())
                .createdById(todo.getCreatedBy().getId())
                .createdByName(todo.getCreatedBy().getName())
                .assigneeMemberId(primaryAssigneeId)
                .assigneeMemberUserName(primaryAssigneeName)
                .assignees(assignees)
                .positionIds(positionIds)
                .representativeColorHex(representativeColorHex)
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }

    /**
     * 투두 변경 알림 전송
     * @param todo 투두
     * @param excludeUserId 알림을 보내지 않을 사용자 ID (생성자/수정자)
     */
    private void sendTodoChangeNotification(Todo todo, Long excludeUserId) {
        if (todo.getAssignees() == null || todo.getAssignees().isEmpty()) {
            return;
        }

        Long teamId = todo.getTeam().getId();
        String todoTitle = todo.getTitle();
        String teamName = todo.getTeam().getName();

        // 담당자 중 알림을 받을 사용자 목록 수집
        List<String> deviceTokens = new ArrayList<>();
        for (TodoAttendee attendee : todo.getAssignees()) {
            Long userId = attendee.getMember().getUser().getId();
            
            // 생성자/수정자 제외
            if (userId.equals(excludeUserId)) {
                continue;
            }

            // 알림 설정 확인
            boolean shouldNotify = notificationSettingRepository.findByUserIdAndTeamId(userId, teamId)
                    .map(setting -> Boolean.TRUE.equals(setting.getEnableTeamAlarm()) &&
                            Boolean.TRUE.equals(setting.getEnableTodoChangeNotification()))
                    .orElse(true); // 설정이 없으면 기본값으로 알림 전송

            if (shouldNotify) {
                deviceTokenService.getDeviceTokenByUserId(userId)
                        .ifPresent(deviceTokens::add);
            }
        }

        if (!deviceTokens.isEmpty()) {
            expoNotificationService.sendTodoChangeNotification(deviceTokens, todoTitle, teamName);
        }
    }
}



package com.example.demo.service;

import com.example.demo.domain.entity.*;
import com.example.demo.domain.enums.TodoStatus;
import com.example.demo.domain.enums.RepeatType;
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

import java.time.LocalDate;
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
        if (request.getTeamId() == null || request.getTeamId() <= 0) {
            throw new IllegalArgumentException("TEAM_NOT_FOUND: 유효하지 않은 팀 ID입니다.");
        }
        
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("TEAM_NOT_FOUND: 팀을 찾을 수 없습니다."));

        TeamMember creatorMember = teamMemberRepository.findByTeamIdAndUserId(team.getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("FORBIDDEN: 팀원이 아닌 사용자는 투두를 생성할 수 없습니다."));

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 반복 설정 검증 및 변환
        String normalizedRepeatType = normalizeRepeatType(request.getRepeatType());
        validateRepeatConfig(normalizedRepeatType,
                request.getRepeatInterval(),
                request.getRepeatEndDate(),
                request.getRepeatUseDate(),
                request.getRepeatWeekDays());

        Integer[] repeatDays = null;
        Integer repeatMonthDay = null;
        if (!normalizedRepeatType.equals(RepeatType.NONE.name())) {
            // 주간 반복 요일 변환 (endAt 기준)
            repeatDays = convertWeekDaysToNumbers(request.getRepeatWeekDays(), normalizedRepeatType, request.getEndAt());

            // MONTHLY/YEARLY + repeatUseDate=true 이면 날짜 사용
            if (Boolean.TRUE.equals(request.getRepeatUseDate()) &&
                    (normalizedRepeatType.equals(RepeatType.MONTHLY.name()) || normalizedRepeatType.equals(RepeatType.YEARLY.name()))) {
                repeatMonthDay = request.getEndAt().getDayOfMonth();
            }
        }

        Todo todo = Todo.builder()
                .team(team)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TodoStatus.TODO)
                .endAt(request.getEndAt())
                .place(request.getPlace())
                .url(request.getUrl())
                .createdBy(creator)
                .repeatType(normalizedRepeatType)
                .repeatInterval(request.getRepeatInterval() == null ? 1 : request.getRepeatInterval())
                .repeatDays(repeatDays)
                .repeatMonthDay(repeatMonthDay)
                .repeatEndDate(toRepeatEndDate(request.getRepeatEndDate()))
                .build();

        todo = todoRepository.save(todo);

        // 담당자 설정 (없으면 생성자 기본 담당자)
        List<Long> assigneeMemberIds = request.getAssigneeMemberIds();
        List<TodoAttendee> assignees = new ArrayList<>();
        if (assigneeMemberIds == null || assigneeMemberIds.isEmpty()) {
            assignees.add(buildTodoAttendee(todo, creatorMember));
        } else {
            // 0이나 null 값 필터링
            List<Long> validMemberIds = assigneeMemberIds.stream()
                    .filter(id -> id != null && id > 0)
                    .distinct()
                    .toList();
            
            if (validMemberIds.isEmpty()) {
                // 유효한 ID가 없으면 생성자를 기본 담당자로 설정
                assignees.add(buildTodoAttendee(todo, creatorMember));
            } else {
                List<TeamMember> members = teamMemberRepository.findAllById(validMemberIds);
                
                // 요청한 ID와 조회된 멤버 수가 다르면 일부 ID가 유효하지 않음
                if (members.size() != validMemberIds.size()) {
                    throw new IllegalArgumentException("INVALID_MEMBER_IDS: 일부 담당자 멤버 ID가 유효하지 않습니다.");
                }
                
                // 모든 멤버가 해당 팀에 속하는지 확인
                for (TeamMember member : members) {
                    if (!member.getTeam().getId().equals(team.getId())) {
                        throw new IllegalArgumentException("INVALID_MEMBER_IDS: 담당자 멤버가 해당 팀에 속하지 않습니다.");
                    }
                    assignees.add(buildTodoAttendee(todo, member));
                }
            }
        }
        todoAttendeeRepository.saveAll(assignees);

        // 포지션 설정
        if (request.getPositionIds() != null && !request.getPositionIds().isEmpty()) {
            // 0이나 null 값 필터링
            List<Long> validPositionIds = request.getPositionIds().stream()
                    .filter(id -> id != null && id > 0)
                    .distinct()
                    .toList();
            
            if (!validPositionIds.isEmpty()) {
                List<Position> positions = positionRepository.findAllById(validPositionIds);
                
                // 요청한 ID와 조회된 포지션 수가 다르면 일부 ID가 유효하지 않음
                if (positions.size() != validPositionIds.size()) {
                    throw new IllegalArgumentException("INVALID_POSITION_IDS: 일부 포지션 ID가 유효하지 않습니다.");
                }
                
                // 모든 포지션이 해당 팀에 속하는지 확인
                List<TodoPosition> todoPositions = new ArrayList<>();
                for (int i = 0; i < positions.size(); i++) {
                    Position position = positions.get(i);
                    if (!position.getTeam().getId().equals(team.getId())) {
                        throw new IllegalArgumentException("INVALID_POSITION_IDS: 포지션이 해당 팀에 속하지 않습니다.");
                    }
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

        // 투두 생성 알림 전송 (생성자 제외)
        sendTodoChangeNotification(todo, userId);

        return toResponse(todo);
    }

    @Transactional
    public TodoResponseDto updateTodo(Long userId, Long todoId, TodoUpdateRequest request) {
        // 투두 조회 (없으면 404)
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("TODO_NOT_FOUND: 투두를 찾을 수 없습니다."));

        // 권한 확인 (팀원이 아니면 403)
        if (!teamMemberRepository.existsByTeamIdAndUserId(todo.getTeam().getId(), userId)) {
            throw new IllegalArgumentException("FORBIDDEN: 팀원이 아닌 사용자는 투두를 수정할 수 없습니다.");
        }

        if (request.getTitle() != null) todo.setTitle(request.getTitle());
        if (request.getDescription() != null) todo.setDescription(request.getDescription());
        if (request.getStatus() != null) todo.setStatus(request.getStatus());
        if (request.getEndAt() != null) todo.setEndAt(request.getEndAt());
        if (request.getPlace() != null) todo.setPlace(request.getPlace());
        if (request.getUrl() != null) todo.setUrl(request.getUrl());

        // 반복 설정 업데이트
        if (request.getRepeatType() != null ||
                request.getRepeatInterval() != null ||
                request.getRepeatEndDate() != null ||
                request.getRepeatUseDate() != null ||
                request.getRepeatWeekDays() != null) {

            String normalizedRepeatType = normalizeRepeatType(request.getRepeatType() != null
                    ? request.getRepeatType()
                    : todo.getRepeatType());

            validateRepeatConfig(normalizedRepeatType,
                    request.getRepeatInterval(),
                    request.getRepeatEndDate(),
                    request.getRepeatUseDate(),
                    request.getRepeatWeekDays());

            todo.setRepeatType(normalizedRepeatType);

            if (request.getRepeatInterval() != null) {
                todo.setRepeatInterval(request.getRepeatInterval());
            }

            if (!normalizedRepeatType.equals(RepeatType.NONE.name())) {
                if (request.getRepeatWeekDays() != null) {
                    Integer[] repeatDays = convertWeekDaysToNumbers(
                            request.getRepeatWeekDays(),
                            normalizedRepeatType,
                            todo.getEndAt());
                    todo.setRepeatDays(repeatDays);
                }

                if (request.getRepeatEndDate() != null) {
                    todo.setRepeatEndDate(toRepeatEndDate(request.getRepeatEndDate()));
                }

                if (Boolean.TRUE.equals(request.getRepeatUseDate()) &&
                        (normalizedRepeatType.equals(RepeatType.MONTHLY.name()) || normalizedRepeatType.equals(RepeatType.YEARLY.name()))) {
                    Integer repeatMonthDay = todo.getEndAt() != null ? todo.getEndAt().getDayOfMonth() : null;
                    todo.setRepeatMonthDay(repeatMonthDay);
                }
            } else {
                // NONE이면 반복 관련 필드 초기화
                todo.setRepeatDays(null);
                todo.setRepeatMonthDay(null);
                todo.setRepeatEndDate(null);
                todo.setRepeatInterval(1);
            }
        }

        if (request.getAssigneeMemberIds() != null) {
            // 0이나 null 값 필터링
            List<Long> validMemberIds = request.getAssigneeMemberIds().stream()
                    .filter(id -> id != null && id > 0)
                    .distinct()
                    .toList();
            
            if (!validMemberIds.isEmpty()) {
                List<TeamMember> members = teamMemberRepository.findAllById(validMemberIds);
                
                // 요청한 ID와 조회된 멤버 수가 다르면 일부 ID가 유효하지 않음
                if (members.size() != validMemberIds.size()) {
                    throw new IllegalArgumentException("INVALID_MEMBER_IDS: 일부 담당자 멤버 ID가 유효하지 않습니다.");
                }
                
                // 모든 멤버가 해당 팀에 속하는지 확인
                Long teamId = todo.getTeam().getId();
                for (TeamMember member : members) {
                    if (!member.getTeam().getId().equals(teamId)) {
                        throw new IllegalArgumentException("INVALID_MEMBER_IDS: 담당자 멤버가 해당 팀에 속하지 않습니다.");
                    }
                }
                
                todo.getAssignees().clear();
                for (TeamMember member : members) {
                    todo.getAssignees().add(buildTodoAttendee(todo, member));
                }
            } else {
                // 유효한 멤버 ID가 없으면 담당자 목록 비우기
                todo.getAssignees().clear();
            }
        }

        if (request.getPositionIds() != null) {
            // 0이나 null 값 필터링
            List<Long> validPositionIds = request.getPositionIds().stream()
                    .filter(id -> id != null && id > 0)
                    .distinct()
                    .toList();
            
            if (!validPositionIds.isEmpty()) {
                List<Position> positions = positionRepository.findAllById(validPositionIds);
                
                // 요청한 ID와 조회된 포지션 수가 다르면 일부 ID가 유효하지 않음
                if (positions.size() != validPositionIds.size()) {
                    throw new IllegalArgumentException("INVALID_POSITION_IDS: 일부 포지션 ID가 유효하지 않습니다.");
                }
                
                // 모든 포지션이 해당 팀에 속하는지 확인
                Long teamId = todo.getTeam().getId();
                for (Position position : positions) {
                    if (!position.getTeam().getId().equals(teamId)) {
                        throw new IllegalArgumentException("INVALID_POSITION_IDS: 포지션이 해당 팀에 속하지 않습니다.");
                    }
                }
                
                todo.getPositions().clear();
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
            } else {
                // 유효한 포지션 ID가 없으면 포지션 목록 비우기
                todo.getPositions().clear();
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
        // 투두 조회 (없으면 404)
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("TODO_NOT_FOUND: 투두를 찾을 수 없습니다."));

        // 권한 확인 (팀원이 아니면 403)
        if (!teamMemberRepository.existsByTeamIdAndUserId(todo.getTeam().getId(), userId)) {
            throw new IllegalArgumentException("FORBIDDEN: 팀원이 아닌 사용자는 투두를 수정할 수 없습니다.");
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
        // 투두 조회 (없으면 404)
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("TODO_NOT_FOUND: 투두를 찾을 수 없습니다."));

        // 권한 확인 (팀원이 아니면 403)
        if (!teamMemberRepository.existsByTeamIdAndUserId(todo.getTeam().getId(), userId)) {
            throw new IllegalArgumentException("FORBIDDEN: 팀원이 아닌 사용자는 투두를 삭제할 수 없습니다.");
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

                    // 반복 설정 필드 조회 (엔티티 직접 로드)
                    Todo todoEntity = todoRepository.findById(response.getId()).orElse(null);
                    String repeatType = null;
                    Integer repeatInterval = null;
                    LocalDate repeatEndDate = null;
                    Boolean repeatUseDate = null;
                    List<String> repeatWeekDays = null;

                    if (todoEntity != null) {
                        repeatType = todoEntity.getRepeatType();
                        repeatInterval = todoEntity.getRepeatInterval();
                        Integer[] repeatDaysArray = todoEntity.getRepeatDays();
                        Integer repeatMonthDay = todoEntity.getRepeatMonthDay();
                        LocalDateTime repeatEndDateDateTime = todoEntity.getRepeatEndDate();
                        repeatEndDate = repeatEndDateDateTime != null ? repeatEndDateDateTime.toLocalDate() : null;

                        if (repeatDaysArray != null && repeatDaysArray.length > 0) {
                            repeatWeekDays = convertNumbersToWeekDays(repeatDaysArray);
                        }

                        if (repeatMonthDay != null) {
                            repeatUseDate = true;
                        }
                    }

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
                            .repeatType(repeatType)
                            .repeatInterval(repeatInterval)
                            .repeatEndDate(repeatEndDate)
                            .repeatUseDate(repeatUseDate)
                            .repeatWeekDays(repeatWeekDays)
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
                .repeatType(todo.getRepeatType())
                .repeatInterval(todo.getRepeatInterval())
                .repeatEndDate(todo.getRepeatEndDate() == null ? null : todo.getRepeatEndDate().toLocalDate())
                .repeatUseDate(todo.getRepeatMonthDay() != null ? true : null)
                .repeatWeekDays(todo.getRepeatDays() != null ? convertNumbersToWeekDays(todo.getRepeatDays()) : null)
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }

    // ===================== 반복 설정 공통 유틸 =====================

    private String normalizeRepeatType(String repeatType) {
        if (repeatType == null || repeatType.isBlank()) {
            return RepeatType.NONE.name();
        }
        try {
            return RepeatType.valueOf(repeatType.toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("INVALID_REPEAT_TYPE: 유효하지 않은 반복 유형입니다. 가능한 값: NONE, DAILY, WEEKLY, MONTHLY, YEARLY (입력값: %s)", repeatType)
            );
        }
    }

    private void validateRepeatConfig(String normalizedRepeatType,
                                      Integer repeatInterval,
                                      LocalDate repeatEndDate,
                                      Boolean repeatUseDate,
                                      List<String> repeatWeekDays) {
        // NONE인데 다른 반복 필드들이 들어오면 에러
        if (RepeatType.NONE.name().equals(normalizedRepeatType)) {
            if (repeatInterval != null || repeatEndDate != null ||
                    (repeatUseDate != null && repeatUseDate) ||
                    (repeatWeekDays != null && !repeatWeekDays.isEmpty())) {
                throw new IllegalArgumentException("INVALID_REPEAT_CONFIG: repeatType이 NONE일 때는 반복 관련 필드를 보낼 수 없습니다.");
            }
        }

        // interval이 0 이하이면 에러
        if (repeatInterval != null && repeatInterval <= 0) {
            throw new IllegalArgumentException("INVALID_REPEAT_CONFIG: repeatInterval은 1 이상이어야 합니다.");
        }
    }

    private Integer[] convertWeekDaysToNumbers(List<String> weekDays, String repeatType, LocalDateTime endAt) {
        if (weekDays == null || weekDays.isEmpty()) {
            // 매주 반복인데 요일이 지정되지 않은 경우, 종료일의 요일을 기본값으로 사용
            if (repeatType != null && repeatType.equals(RepeatType.WEEKLY.name())) {
                int dayOfWeekValue = endAt.getDayOfWeek().getValue(); // 1=월요일, 7=일요일
                int zeroBasedDay = dayOfWeekValue == 7 ? 0 : dayOfWeekValue; // 0=일요일, 1=월요일, ...
                return new Integer[]{zeroBasedDay};
            }
            return null;
        }

        java.util.Map<String, Integer> weekDayMap = new java.util.HashMap<>();
        weekDayMap.put("일", 0);
        weekDayMap.put("월", 1);
        weekDayMap.put("화", 2);
        weekDayMap.put("수", 3);
        weekDayMap.put("목", 4);
        weekDayMap.put("금", 5);
        weekDayMap.put("토", 6);

        return weekDays.stream()
                .map(weekDayMap::get)
                .filter(java.util.Objects::nonNull)
                .toArray(Integer[]::new);
    }

    private List<String> convertNumbersToWeekDays(Integer[] numbers) {
        if (numbers == null || numbers.length == 0) {
            return null;
        }

        String[] weekDays = {"일", "월", "화", "수", "목", "금", "토"};
        return java.util.Arrays.stream(numbers)
                .filter(n -> n >= 0 && n < 7)
                .map(n -> weekDays[n])
                .collect(java.util.stream.Collectors.toList());
    }

    private LocalDateTime toRepeatEndDate(LocalDate repeatEndDate) {
        if (repeatEndDate == null) return null;
        return repeatEndDate.atTime(23, 59, 59);
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



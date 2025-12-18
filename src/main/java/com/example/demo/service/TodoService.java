package com.example.demo.service;

import com.example.demo.domain.entity.*;
import com.example.demo.domain.enums.TodoStatus;
import com.example.demo.dto.todo.TodoAssigneeDto;
import com.example.demo.dto.todo.TodoCreateRequest;
import com.example.demo.dto.todo.TodoResponse;
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

    @Transactional
    public TodoResponse createTodo(Long userId, TodoCreateRequest request) {
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

        return toResponse(todo);
    }

    @Transactional
    public TodoResponse updateTodo(Long userId, Long todoId, TodoUpdateRequest request) {
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
    public List<TodoResponse> getTeamTodos(Long userId, Long teamId, LocalDateTime start, LocalDateTime end) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new IllegalArgumentException("팀원이 아닌 사용자는 팀 투두를 조회할 수 없습니다.");
        }
        List<Todo> todos = todoRepository.findByTeamAndRange(teamId, start, end);
        return todos.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getMyTodos(Long userId, LocalDateTime start, LocalDateTime end) {
        List<TeamMember> members = teamMemberRepository.findByUserId(userId);
        List<Long> memberIds = members.stream().map(TeamMember::getId).toList();
        if (memberIds.isEmpty()) {
            return List.of();
        }
        List<Todo> todos = todoRepository.findByAssigneesAndRange(memberIds, start, end);
        return todos.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private TodoAttendee buildTodoAttendee(Todo todo, TeamMember member) {
        TodoAttendeeId id = new TodoAttendeeId(todo.getId(), member.getId());
        return TodoAttendee.builder()
                .id(id)
                .todo(todo)
                .member(member)
                .build();
    }

    private TodoResponse toResponse(Todo todo) {
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

        Long representativePositionId = positionIds.isEmpty() ? null : positionIds.get(0);

        return TodoResponse.builder()
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
                .representativePositionId(representativePositionId)
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }
}



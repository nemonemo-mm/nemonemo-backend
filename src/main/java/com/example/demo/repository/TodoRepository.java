package com.example.demo.repository;

import com.example.demo.domain.entity.Todo;
import com.example.demo.dto.todo.TodoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("""
            select 
                t.id as id,
                t.team.id as teamId,
                t.team.name as teamName,
                t.title as title,
                t.description as description,
                t.status as status,
                t.endAt as endAt,
                t.place as place,
                t.url as url,
                t.createdBy.id as createdById,
                t.createdBy.name as createdByName,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt
            from Todo t
            where t.team.id = :teamId
              and (
                    (COALESCE(t.repeatType, 'NONE') = 'NONE' and t.endAt <= :end and t.endAt >= :start)
                 or (COALESCE(t.repeatType, 'NONE') <> 'NONE' and (t.repeatEndDate is null or t.repeatEndDate >= :start))
              )
            """)
    List<TodoResponse> findByTeamAndRange(
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select distinct
                t.id as id,
                t.team.id as teamId,
                t.team.name as teamName,
                t.title as title,
                t.description as description,
                t.status as status,
                t.endAt as endAt,
                t.place as place,
                t.url as url,
                t.createdBy.id as createdById,
                t.createdBy.name as createdByName,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt
            from Todo t
            join t.assignees a
            where a.member.id in :memberIds
              and (
                    (COALESCE(t.repeatType, 'NONE') = 'NONE' and t.endAt <= :end and t.endAt >= :start)
                 or (COALESCE(t.repeatType, 'NONE') <> 'NONE' and (t.repeatEndDate is null or t.repeatEndDate >= :start))
              )
            """)
    List<TodoResponse> findByAssigneesAndRange(
            @Param("memberIds") List<Long> memberIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select ta.member.id
            from TodoAttendee ta
            where ta.todo.id = :todoId
            """)
    List<Long> findAssigneeMemberIdsByTodoId(@Param("todoId") Long todoId);

    @Query("""
            select ta.member.id, ta.member.user.name
            from TodoAttendee ta
            where ta.todo.id = :todoId
            """)
    List<Object[]> findAssigneeInfoByTodoId(@Param("todoId") Long todoId);

    @Query("""
            select tp.position.id
            from TodoPosition tp
            where tp.todo.id = :todoId
            order by tp.orderIndex
            """)
    List<Long> findPositionIdsByTodoId(@Param("todoId") Long todoId);

    /**
     * 마감 시간이 가까운 투두 조회 (알림 스케줄러용)
     * TODO 상태이고, endAt이 지정된 시간 범위 내에 있는 투두를 조회합니다.
     */
    @Query("""
            select t
            from Todo t
            where t.status = 'TODO'
              and t.endAt > :start
              and t.endAt < :end
            """)
    List<Todo> findUpcomingTodosForNotification(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}



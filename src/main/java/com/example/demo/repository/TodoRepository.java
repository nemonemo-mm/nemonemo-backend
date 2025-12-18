package com.example.demo.repository;

import com.example.demo.domain.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("select t from Todo t where t.team.id = :teamId and t.endAt <= :end and t.endAt >= :start")
    List<Todo> findByTeamAndRange(
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select distinct t
            from Todo t
            join t.assignees a
            where a.member.id in :memberIds
              and t.endAt <= :end and t.endAt >= :start
            """)
    List<Todo> findByAssigneesAndRange(
            @Param("memberIds") List<Long> memberIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}



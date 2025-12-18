package com.example.demo.repository;

import com.example.demo.domain.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("select s from Schedule s where s.team.id = :teamId and s.startAt < :end and s.endAt > :start")
    List<Schedule> findByTeamAndRange(
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select distinct s 
            from Schedule s
            join s.attendees a
            where a.member.id in :memberIds
              and s.startAt < :end and s.endAt > :start
            """)
    List<Schedule> findByAttendeesAndRange(
            @Param("memberIds") List<Long> memberIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select distinct s 
            from Schedule s
            join s.positions p
            where s.team.id = :teamId
              and p.position.id in :positionIds
              and s.startAt < :end and s.endAt > :start
            """)
    List<Schedule> findByTeamAndPositionsAndRange(
            @Param("teamId") Long teamId,
            @Param("positionIds") List<Long> positionIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select distinct s 
            from Schedule s
            join s.attendees a
            join s.positions p
            where a.member.id in :memberIds
              and p.position.id in :positionIds
              and s.startAt < :end and s.endAt > :start
            """)
    List<Schedule> findByAttendeesAndPositionsAndRange(
            @Param("memberIds") List<Long> memberIds,
            @Param("positionIds") List<Long> positionIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select distinct s 
            from Schedule s
            join s.attendees a
            where a.member.id in :memberIds
              and s.team.id = :teamId
              and s.startAt < :end and s.endAt > :start
            """)
    List<Schedule> findByAttendeesAndTeamAndRange(
            @Param("memberIds") List<Long> memberIds,
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select distinct s 
            from Schedule s
            join s.attendees a
            join s.positions p
            where a.member.id in :memberIds
              and s.team.id = :teamId
              and p.position.id in :positionIds
              and s.startAt < :end and s.endAt > :start
            """)
    List<Schedule> findByAttendeesAndTeamAndPositionsAndRange(
            @Param("memberIds") List<Long> memberIds,
            @Param("teamId") Long teamId,
            @Param("positionIds") List<Long> positionIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}



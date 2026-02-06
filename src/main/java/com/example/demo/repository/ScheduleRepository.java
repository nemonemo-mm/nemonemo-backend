package com.example.demo.repository;

import com.example.demo.domain.entity.Schedule;
import com.example.demo.dto.schedule.ScheduleResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("""
            select 
                s.id as id,
                s.team.id as teamId,
                s.team.name as teamName,
                s.title as title,
                s.description as description,
                s.startAt as startAt,
                s.endAt as endAt,
                s.isAllDay as isAllDay,
                s.place as place,
                s.url as url,
                s.createdBy.id as createdById,
                s.createdBy.name as createdByName,
                s.createdAt as createdAt,
                s.updatedAt as updatedAt,
                s.parentSchedule.id as parentScheduleId
            from Schedule s
            where s.team.id = :teamId
              and (
                    (s.repeatType = 'NONE' and s.startAt < :end and s.endAt > :start)
                 or (s.repeatType <> 'NONE' and (s.repeatEndDate is null or s.repeatEndDate >= :start))
              )
            """)
    List<ScheduleResponse> findByTeamAndRange(
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select distinct
                s.id as id,
                s.team.id as teamId,
                s.team.name as teamName,
                s.title as title,
                s.description as description,
                s.startAt as startAt,
                s.endAt as endAt,
                s.isAllDay as isAllDay,
                s.place as place,
                s.url as url,
                s.createdBy.id as createdById,
                s.createdBy.name as createdByName,
                s.createdAt as createdAt,
                s.updatedAt as updatedAt,
                s.parentSchedule.id as parentScheduleId
            from Schedule s
            join s.attendees a
            where a.member.id in :memberIds
              and (
                    (s.repeatType = 'NONE' and s.startAt < :end and s.endAt > :start)
                 or (s.repeatType <> 'NONE' and (s.repeatEndDate is null or s.repeatEndDate >= :start))
              )
            """)
    List<ScheduleResponse> findByAttendeesAndRange(
            @Param("memberIds") List<Long> memberIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select distinct
                s.id as id,
                s.team.id as teamId,
                s.team.name as teamName,
                s.title as title,
                s.description as description,
                s.startAt as startAt,
                s.endAt as endAt,
                s.isAllDay as isAllDay,
                s.place as place,
                s.url as url,
                s.createdBy.id as createdById,
                s.createdBy.name as createdByName,
                s.createdAt as createdAt,
                s.updatedAt as updatedAt,
                s.parentSchedule.id as parentScheduleId
            from Schedule s
            join s.positions p
            where s.team.id = :teamId
              and p.position.id in :positionIds
              and (
                    (s.repeatType = 'NONE' and s.startAt < :end and s.endAt > :start)
                 or (s.repeatType <> 'NONE' and (s.repeatEndDate is null or s.repeatEndDate >= :start))
              )
            """)
    List<ScheduleResponse> findByTeamAndPositionsAndRange(
            @Param("teamId") Long teamId,
            @Param("positionIds") List<Long> positionIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select distinct
                s.id as id,
                s.team.id as teamId,
                s.team.name as teamName,
                s.title as title,
                s.description as description,
                s.startAt as startAt,
                s.endAt as endAt,
                s.isAllDay as isAllDay,
                s.place as place,
                s.url as url,
                s.createdBy.id as createdById,
                s.createdBy.name as createdByName,
                s.createdAt as createdAt,
                s.updatedAt as updatedAt,
                s.parentSchedule.id as parentScheduleId
            from Schedule s
            join s.attendees a
            join s.positions p
            where a.member.id in :memberIds
              and p.position.id in :positionIds
              and (
                    (s.repeatType = 'NONE' and s.startAt < :end and s.endAt > :start)
                 or (s.repeatType <> 'NONE' and (s.repeatEndDate is null or s.repeatEndDate >= :start))
              )
            """)
    List<ScheduleResponse> findByAttendeesAndPositionsAndRange(
            @Param("memberIds") List<Long> memberIds,
            @Param("positionIds") List<Long> positionIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select distinct
                s.id as id,
                s.team.id as teamId,
                s.team.name as teamName,
                s.title as title,
                s.description as description,
                s.startAt as startAt,
                s.endAt as endAt,
                s.isAllDay as isAllDay,
                s.place as place,
                s.url as url,
                s.createdBy.id as createdById,
                s.createdBy.name as createdByName,
                s.createdAt as createdAt,
                s.updatedAt as updatedAt,
                s.parentSchedule.id as parentScheduleId
            from Schedule s
            join s.attendees a
            where a.member.id in :memberIds
              and s.team.id = :teamId
              and (
                    (s.repeatType = 'NONE' and s.startAt < :end and s.endAt > :start)
                 or (s.repeatType <> 'NONE' and (s.repeatEndDate is null or s.repeatEndDate >= :start))
              )
            """)
    List<ScheduleResponse> findByAttendeesAndTeamAndRange(
            @Param("memberIds") List<Long> memberIds,
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select distinct
                s.id as id,
                s.team.id as teamId,
                s.team.name as teamName,
                s.title as title,
                s.description as description,
                s.startAt as startAt,
                s.endAt as endAt,
                s.isAllDay as isAllDay,
                s.place as place,
                s.url as url,
                s.createdBy.id as createdById,
                s.createdBy.name as createdByName,
                s.createdAt as createdAt,
                s.updatedAt as updatedAt,
                s.parentSchedule.id as parentScheduleId
            from Schedule s
            join s.attendees a
            join s.positions p
            where a.member.id in :memberIds
              and s.team.id = :teamId
              and p.position.id in :positionIds
              and (
                    (s.repeatType = 'NONE' and s.startAt < :end and s.endAt > :start)
                 or (s.repeatType <> 'NONE' and (s.repeatEndDate is null or s.repeatEndDate >= :start))
              )
            """)
    List<ScheduleResponse> findByAttendeesAndTeamAndPositionsAndRange(
            @Param("memberIds") List<Long> memberIds,
            @Param("teamId") Long teamId,
            @Param("positionIds") List<Long> positionIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select sp.position.id
            from SchedulePosition sp
            where sp.schedule.id = :scheduleId
            order by sp.orderIndex
            """)
    List<Long> findPositionIdsByScheduleId(@Param("scheduleId") Long scheduleId);

    @Query("""
            select a.member.id
            from ScheduleAttendee a
            where a.schedule.id = :scheduleId
            """)
    List<Long> findAttendeeMemberIdsByScheduleId(@Param("scheduleId") Long scheduleId);

    @Query("""
            select s.repeatType, s.repeatInterval, s.repeatDays, s.repeatMonthDay, s.repeatEndDate
            from Schedule s
            where s.id = :scheduleId
            """)
    Object[] findRepeatFieldsByScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * 시작 시간이 가까운 스케줄 조회 (알림 스케줄러용)
     * startAt이 지정된 시간 범위 내에 있고, 반복 일정의 부모인 스케줄을 조회합니다.
     */
    @Query("""
            select s
            from Schedule s
            where s.startAt > :start
              and s.startAt < :end
              and s.parentSchedule is null
            """)
    List<Schedule> findUpcomingSchedulesForNotification(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}



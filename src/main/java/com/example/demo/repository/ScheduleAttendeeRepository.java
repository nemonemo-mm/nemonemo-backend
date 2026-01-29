package com.example.demo.repository;

import com.example.demo.domain.entity.ScheduleAttendee;
import com.example.demo.domain.entity.ScheduleAttendeeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleAttendeeRepository extends JpaRepository<ScheduleAttendee, ScheduleAttendeeId> {

    @Query("""
            select a.member.id
            from ScheduleAttendee a
            where a.schedule.id = :scheduleId
            """)
    List<Long> findMemberIdsByScheduleId(@Param("scheduleId") Long scheduleId);
}



























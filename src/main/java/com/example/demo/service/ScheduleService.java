package com.example.demo.service;

import com.example.demo.domain.entity.*;
import com.example.demo.domain.enums.RepeatType;
import com.example.demo.domain.model.ScheduleRepeatRule;
import com.example.demo.dto.schedule.ScheduleCreateRequest;
import com.example.demo.dto.schedule.ScheduleResponse;
import com.example.demo.dto.schedule.ScheduleUpdateRequest;
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
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PositionRepository positionRepository;
    private final ScheduleAttendeeRepository scheduleAttendeeRepository;
    private final SchedulePositionRepository schedulePositionRepository;
    private final UserRepository userRepository;

    public enum RepeatScope {
        THIS_ONLY, FOLLOWING, ALL
    }

    @Transactional
    public ScheduleResponse createSchedule(Long userId, ScheduleCreateRequest request) {
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        TeamMember creatorMember = teamMemberRepository.findByTeamIdAndUserId(team.getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("팀원이 아닌 사용자는 일정을 생성할 수 없습니다."));

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String normalizedRepeatType = normalizeRepeatType(request.getRepeatType());
        
        Schedule schedule = Schedule.builder()
                .team(team)
                .title(request.getTitle())
                .description(request.getDescription())
                .place(request.getPlace())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .isAllDay(Boolean.TRUE.equals(request.getIsAllDay()))
                .url(request.getUrl())
                .repeatType(normalizedRepeatType)
                .repeatInterval(request.getRepeatInterval() == null ? 1 : request.getRepeatInterval())
                .repeatDays(getRepeatDaysOrDefault(request.getRepeatDays(), normalizedRepeatType, request.getStartAt()))
                .repeatMonthDay(getRepeatMonthDayOrDefault(request.getRepeatMonthDay(), normalizedRepeatType, request.getStartAt()))
                .repeatEndDate(toRepeatEndDate(request.getRepeatEndDate()))
                .createdBy(creator)
                .build();

        schedule = scheduleRepository.save(schedule);

        // 참석자 설정 (없으면 생성자 기본 참석자)
        List<Long> attendeeMemberIds = request.getAttendeeMemberIds();
        List<ScheduleAttendee> attendees = new ArrayList<>();
        if (attendeeMemberIds == null || attendeeMemberIds.isEmpty()) {
            attendees.add(buildScheduleAttendee(schedule, creatorMember));
        } else {
            List<TeamMember> members = teamMemberRepository.findAllById(attendeeMemberIds);
            for (TeamMember member : members) {
                attendees.add(buildScheduleAttendee(schedule, member));
            }
        }
        scheduleAttendeeRepository.saveAll(attendees);

        // 포지션 설정
        if (request.getPositionIds() != null && !request.getPositionIds().isEmpty()) {
            List<Position> positions = positionRepository.findAllById(request.getPositionIds());
            List<SchedulePosition> schedulePositions = new ArrayList<>();
            for (int i = 0; i < positions.size(); i++) {
                Position position = positions.get(i);
                SchedulePosition sp = SchedulePosition.builder()
                        .id(new SchedulePositionId(schedule.getId(), position.getId()))
                        .schedule(schedule)
                        .position(position)
                        .orderIndex(i)
                        .build();
                schedulePositions.add(sp);
            }
            schedulePositionRepository.saveAll(schedulePositions);
        }

        return toResponse(schedule);
    }

    @Transactional
    public ScheduleResponse updateSchedule(Long userId, Long scheduleId, ScheduleUpdateRequest request, RepeatScope scope) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        // 팀 멤버 검증
        if (!teamMemberRepository.existsByTeamIdAndUserId(schedule.getTeam().getId(), userId)) {
            throw new IllegalArgumentException("팀원이 아닌 사용자는 일정을 수정할 수 없습니다.");
        }

        // 간단화를 위해 현재 버전에서는 ALL만 처리, 기타 scope는 추후 확장
        if (scope == null || scope == RepeatScope.ALL) {
            applyScheduleUpdate(schedule, request);
            scheduleRepository.save(schedule);
        } else {
            // THIS_ONLY / FOLLOWING 에 대한 상세 분리 로직은 추후 확장
            applyScheduleUpdate(schedule, request);
            scheduleRepository.save(schedule);
        }

        return toResponse(schedule);
    }

    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId, RepeatScope scope) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        if (!teamMemberRepository.existsByTeamIdAndUserId(schedule.getTeam().getId(), userId)) {
            throw new IllegalArgumentException("팀원이 아닌 사용자는 일정을 삭제할 수 없습니다.");
        }

        // 단순 구현: scope에 상관없이 해당 스케줄만 삭제
        scheduleRepository.delete(schedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getTeamSchedules(
            Long userId,
            Long teamId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new IllegalArgumentException("팀원이 아닌 사용자는 팀 일정을 조회할 수 없습니다.");
        }
        List<Schedule> schedules = scheduleRepository.findByTeamAndRange(teamId, start, end);
        return schedules.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMySchedules(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        List<TeamMember> members = teamMemberRepository.findByUserId(userId);
        List<Long> memberIds = members.stream().map(TeamMember::getId).toList();
        if (memberIds.isEmpty()) {
            return List.of();
        }
        List<Schedule> schedules = scheduleRepository.findByAttendeesAndRange(memberIds, start, end);
        return schedules.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void applyScheduleUpdate(Schedule schedule, ScheduleUpdateRequest request) {
        if (request.getTitle() != null) schedule.setTitle(request.getTitle());
        if (request.getDescription() != null) schedule.setDescription(request.getDescription());
        if (request.getStartAt() != null) schedule.setStartAt(request.getStartAt());
        if (request.getEndAt() != null) schedule.setEndAt(request.getEndAt());
        if (request.getIsAllDay() != null) schedule.setIsAllDay(request.getIsAllDay());
        if (request.getPlace() != null) schedule.setPlace(request.getPlace());
        if (request.getUrl() != null) schedule.setUrl(request.getUrl());

        if (request.getRepeatType() != null) schedule.setRepeatType(normalizeRepeatType(request.getRepeatType()));
        if (request.getRepeatInterval() != null) schedule.setRepeatInterval(request.getRepeatInterval());
        if (request.getRepeatDays() != null) {
            schedule.setRepeatDays(request.getRepeatDays().toArray(new Integer[0]));
        }
        if (request.getRepeatMonthDay() != null) schedule.setRepeatMonthDay(request.getRepeatMonthDay());
        if (request.getRepeatEndDate() != null) schedule.setRepeatEndDate(toRepeatEndDate(request.getRepeatEndDate()));

        // 참석자 및 포지션 갱신은 단순화를 위해 전체 교체
        if (request.getAttendeeMemberIds() != null) {
            schedule.getAttendees().clear();
            List<TeamMember> members = teamMemberRepository.findAllById(request.getAttendeeMemberIds());
            for (TeamMember member : members) {
                schedule.getAttendees().add(buildScheduleAttendee(schedule, member));
            }
        }

        if (request.getPositionIds() != null) {
            schedule.getPositions().clear();
            List<Position> positions = positionRepository.findAllById(request.getPositionIds());
            int index = 0;
            for (Position position : positions) {
                SchedulePosition sp = SchedulePosition.builder()
                        .id(new SchedulePositionId(schedule.getId(), position.getId()))
                        .schedule(schedule)
                        .position(position)
                        .orderIndex(index++)
                        .build();
                schedule.getPositions().add(sp);
            }
        }
    }

    private ScheduleAttendee buildScheduleAttendee(Schedule schedule, TeamMember member) {
        ScheduleAttendeeId id = new ScheduleAttendeeId(schedule.getId(), member.getId());
        return ScheduleAttendee.builder()
                .id(id)
                .schedule(schedule)
                .member(member)
                .build();
    }

    private LocalDateTime toRepeatEndDate(LocalDate repeatEndDate) {
        if (repeatEndDate == null) return null;
        return repeatEndDate.atTime(23, 59, 59);
        }

    private String normalizeRepeatType(String repeatType) {
        if (repeatType == null || repeatType.isBlank()) {
            return RepeatType.NONE.name();
        }
        return RepeatType.valueOf(repeatType).name();
    }

    private Integer[] getRepeatDaysOrDefault(List<Integer> repeatDays, String repeatType, LocalDateTime startAt) {
        if (repeatDays != null && !repeatDays.isEmpty()) {
            return repeatDays.toArray(new Integer[0]);
        }
        // 매주 반복인데 요일이 지정되지 않은 경우, 시작일의 요일을 기본값으로 사용
        if (repeatType != null && repeatType.equals(RepeatType.WEEKLY.name())) {
            // DayOfWeek: MONDAY=1, TUESDAY=2, ..., SUNDAY=7
            // 요구사항: 0=일요일, 1=월요일, ...
            int dayOfWeekValue = startAt.getDayOfWeek().getValue(); // 1=월요일, 7=일요일
            int zeroBasedDay = dayOfWeekValue == 7 ? 0 : dayOfWeekValue; // 0=일요일, 1=월요일, ...
            return new Integer[]{zeroBasedDay};
        }
        return null;
    }

    private Integer getRepeatMonthDayOrDefault(Integer repeatMonthDay, String repeatType, LocalDateTime startAt) {
        if (repeatMonthDay != null) {
            return repeatMonthDay;
        }
        // 매월 반복인데 날짜가 지정되지 않은 경우, 시작일의 날짜를 기본값으로 사용
        if (repeatType != null && repeatType.equals(RepeatType.MONTHLY.name())) {
            return startAt.getDayOfMonth();
        }
        return null;
    }

    private ScheduleResponse toResponse(Schedule schedule) {
        ScheduleRepeatRule rule = ScheduleRepeatRule.fromEntityFields(
                schedule.getRepeatType(),
                schedule.getRepeatInterval(),
                schedule.getRepeatDays(),
                schedule.getRepeatMonthDay(),
                schedule.getRepeatEndDate()
        );

        List<SchedulePosition> sortedPositions = schedule.getPositions() == null
                ? List.of()
                : schedule.getPositions().stream()
                .sorted(Comparator.comparing(SchedulePosition::getOrderIndex))
                .toList();

        List<Long> positionIds = sortedPositions.stream()
                .map(sp -> sp.getPosition().getId())
                .toList();

        Long representativePositionId = positionIds.isEmpty() ? null : positionIds.get(0);

        return ScheduleResponse.builder()
                .id(schedule.getId())
                .teamId(schedule.getTeam().getId())
                .teamName(schedule.getTeam().getName())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .startAt(schedule.getStartAt())
                .endAt(schedule.getEndAt())
                .isAllDay(schedule.getIsAllDay())
                .place(schedule.getPlace())
                .url(schedule.getUrl())
                .createdById(schedule.getCreatedBy().getId())
                .createdByName(schedule.getCreatedBy().getName())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .positionIds(positionIds)
                .representativePositionId(representativePositionId)
                .repeatSummary(rule.toSummary())
                .parentScheduleId(schedule.getParentSchedule() == null ? null : schedule.getParentSchedule().getId())
                .build();
    }
}



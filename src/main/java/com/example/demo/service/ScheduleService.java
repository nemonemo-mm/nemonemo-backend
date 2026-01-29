package com.example.demo.service;

import com.example.demo.domain.entity.*;
import com.example.demo.domain.enums.RepeatType;
import com.example.demo.domain.model.ScheduleRepeatRule;
import com.example.demo.dto.schedule.ScheduleCreateRequest;
import com.example.demo.dto.schedule.ScheduleResponse;
import com.example.demo.dto.schedule.ScheduleResponseDto;
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
import java.util.Map;
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
    private final ExpoNotificationService expoNotificationService;
    private final DeviceTokenService deviceTokenService;
    private final NotificationSettingRepository notificationSettingRepository;

    public enum RepeatScope {
        THIS_ONLY, FOLLOWING, ALL
    }

    @Transactional
    public ScheduleResponseDto createSchedule(Long userId, ScheduleCreateRequest request) {
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        TeamMember creatorMember = teamMemberRepository.findByTeamIdAndUserId(team.getId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("팀원이 아닌 사용자는 일정을 생성할 수 없습니다."));

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String normalizedRepeatType = normalizeRepeatType(request.getRepeatType());
        
        // repeatWeekDays를 repeatDays로 변환
        Integer[] repeatDays = convertWeekDaysToNumbers(request.getRepeatWeekDays(), normalizedRepeatType, request.getStartAt());
        // repeatUseDate가 true이고 MONTHLY/YEARLY면 시작일의 날짜를 repeatMonthDay로 설정
        Integer repeatMonthDay = null;
        if (Boolean.TRUE.equals(request.getRepeatUseDate()) && 
            (normalizedRepeatType.equals(RepeatType.MONTHLY.name()) || normalizedRepeatType.equals(RepeatType.YEARLY.name()))) {
            repeatMonthDay = request.getStartAt().getDayOfMonth();
        }
        
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
                .repeatDays(repeatDays)
                .repeatMonthDay(repeatMonthDay)
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
        List<Long> positionIds = request.getPositionIds();
        List<SchedulePosition> schedulePositions = new ArrayList<>();

        if (positionIds == null || positionIds.isEmpty()) {
            // 포지션이 비어있으면 생성자의 포지션을 기본으로 사용
            Position creatorPosition = creatorMember.getPosition();
            if (creatorPosition != null) {
                SchedulePosition sp = SchedulePosition.builder()
                        .id(new SchedulePositionId(schedule.getId(), creatorPosition.getId()))
                        .schedule(schedule)
                        .position(creatorPosition)
                        .orderIndex(0)
                        .build();
                schedulePositions.add(sp);
            }
            // 생성자가 포지션이 없으면 스케줄 포지션도 비워둠 (전체로 처리)
        } else {
            List<Position> positions = positionRepository.findAllById(positionIds);
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
        }

        if (!schedulePositions.isEmpty()) {
            schedulePositionRepository.saveAll(schedulePositions);
        }

        // 스케줄 생성 알림 전송 (생성자 제외)
        sendScheduleChangeNotification(schedule, userId);

        return toResponse(schedule);
    }

    @Transactional
    public ScheduleResponseDto updateSchedule(Long userId, Long scheduleId, ScheduleUpdateRequest request, RepeatScope scope) {
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

        // 스케줄 수정 알림 전송 (수정자 제외)
        sendScheduleChangeNotification(schedule, userId);

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
    public List<ScheduleResponseDto> getTeamSchedules(
            Long userId,
            Long teamId,
            LocalDateTime start,
            LocalDateTime end,
            List<Long> positionIds
    ) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new IllegalArgumentException("팀원이 아닌 사용자는 팀 일정을 조회할 수 없습니다.");
        }
        List<ScheduleResponse> responses;
        if (positionIds != null && !positionIds.isEmpty()) {
            responses = scheduleRepository.findByTeamAndPositionsAndRange(teamId, positionIds, start, end);
        } else {
            responses = scheduleRepository.findByTeamAndRange(teamId, start, end);
        }
        return enrichScheduleResponses(responses);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getMySchedules(
            Long userId,
            LocalDateTime start,
            LocalDateTime end,
            Long teamId,
            List<Long> positionIds
    ) {
        List<TeamMember> members;
        if (teamId != null) {
            // 특정 팀으로 필터링: 해당 팀의 멤버만 조회
            TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 팀의 팀원이 아닙니다."));
            members = List.of(member);
        } else {
            // 전체 팀: 사용자가 속한 모든 팀의 멤버 조회
            members = teamMemberRepository.findByUserId(userId);
        }
        
        List<Long> memberIds = members.stream().map(TeamMember::getId).toList();
        if (memberIds.isEmpty()) {
            return List.of();
        }
        
        List<ScheduleResponse> responses;
        if (teamId != null) {
            // 팀 필터링이 있는 경우
            if (positionIds != null && !positionIds.isEmpty()) {
                responses = scheduleRepository.findByAttendeesAndTeamAndPositionsAndRange(memberIds, teamId, positionIds, start, end);
            } else {
                responses = scheduleRepository.findByAttendeesAndTeamAndRange(memberIds, teamId, start, end);
            }
        } else {
            // 팀 필터링이 없는 경우 (전체 팀)
            if (positionIds != null && !positionIds.isEmpty()) {
                responses = scheduleRepository.findByAttendeesAndPositionsAndRange(memberIds, positionIds, start, end);
            } else {
                responses = scheduleRepository.findByAttendeesAndRange(memberIds, start, end);
            }
        }
        return enrichScheduleResponses(responses);
    }

    private void applyScheduleUpdate(Schedule schedule, ScheduleUpdateRequest request) {
        if (request.getTitle() != null) schedule.setTitle(request.getTitle());
        if (request.getDescription() != null) schedule.setDescription(request.getDescription());
        if (request.getStartAt() != null) schedule.setStartAt(request.getStartAt());
        if (request.getEndAt() != null) schedule.setEndAt(request.getEndAt());
        if (request.getIsAllDay() != null) schedule.setIsAllDay(request.getIsAllDay());
        if (request.getPlace() != null) schedule.setPlace(request.getPlace());
        if (request.getUrl() != null) schedule.setUrl(request.getUrl());

        if (request.getRepeatType() != null) {
            String normalizedRepeatType = normalizeRepeatType(request.getRepeatType());
            schedule.setRepeatType(normalizedRepeatType);
            
            // repeatWeekDays를 repeatDays로 변환
            if (request.getRepeatWeekDays() != null) {
                Integer[] repeatDays = convertWeekDaysToNumbers(request.getRepeatWeekDays(), normalizedRepeatType, schedule.getStartAt());
                schedule.setRepeatDays(repeatDays);
            }
            
            // repeatUseDate가 true이고 MONTHLY/YEARLY면 시작일의 날짜를 repeatMonthDay로 설정
            if (Boolean.TRUE.equals(request.getRepeatUseDate()) && 
                (normalizedRepeatType.equals(RepeatType.MONTHLY.name()) || normalizedRepeatType.equals(RepeatType.YEARLY.name()))) {
                Integer repeatMonthDay = schedule.getStartAt() != null ? schedule.getStartAt().getDayOfMonth() : null;
                schedule.setRepeatMonthDay(repeatMonthDay);
            }
        }
        if (request.getRepeatInterval() != null) schedule.setRepeatInterval(request.getRepeatInterval());
        if (request.getRepeatEndDate() != null) schedule.setRepeatEndDate(toRepeatEndDate(request.getRepeatEndDate()));

        // 참석자 및 포지션 갱신은 단순화를 위해 전체 교체
        if (request.getAttendeeMemberIds() != null) {
            if (schedule.getAttendees() == null) {
                schedule.setAttendees(new ArrayList<>());
            } else {
                schedule.getAttendees().clear();
            }
            List<TeamMember> members = teamMemberRepository.findAllById(request.getAttendeeMemberIds());
            for (TeamMember member : members) {
                schedule.getAttendees().add(buildScheduleAttendee(schedule, member));
            }
        }

        if (request.getPositionIds() != null) {
            if (schedule.getPositions() == null) {
                schedule.setPositions(new ArrayList<>());
            } else {
                schedule.getPositions().clear();
            }
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


    /**
     * 한글 요일 문자열 리스트를 숫자 배열로 변환
     * "월", "화", "수", "목", "금", "토", "일" -> [1, 2, 3, 4, 5, 6, 0]
     */
    private Integer[] convertWeekDaysToNumbers(List<String> weekDays, String repeatType, LocalDateTime startAt) {
        if (weekDays == null || weekDays.isEmpty()) {
            // 매주 반복인데 요일이 지정되지 않은 경우, 시작일의 요일을 기본값으로 사용
            if (repeatType != null && repeatType.equals(RepeatType.WEEKLY.name())) {
                int dayOfWeekValue = startAt.getDayOfWeek().getValue(); // 1=월요일, 7=일요일
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

    /**
     * 숫자 배열을 한글 요일 문자열 리스트로 변환
     * [1, 2, 3] -> ["월", "화", "수"]
     */
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

    private List<ScheduleResponseDto> enrichScheduleResponses(List<ScheduleResponse> responses) {
        return responses.stream()
                .map(response -> {
                    // positionIds 및 대표 포지션 컬러 가져오기
                    List<Long> positionIds = scheduleRepository.findPositionIdsByScheduleId(response.getId());
                    
                    // 포지션 ID와 색상 매핑 생성
                    Map<Long, String> positionColors = null;
                    String representativeColorHex = null;
                    if (!positionIds.isEmpty()) {
                        List<Position> positions = positionRepository.findAllById(positionIds);
                        positionColors = positions.stream()
                                .collect(Collectors.toMap(
                                        Position::getId,
                                        Position::getColorHex,
                                        (existing, replacement) -> existing
                                ));
                        representativeColorHex = positions.stream()
                                .filter(p -> p.getId().equals(positionIds.get(0)))
                                .findFirst()
                                .map(Position::getColorHex)
                                .orElse(null);
                    }

                    // 참석자 팀멤버 ID 목록 가져오기
                    List<Long> attendeeMemberIds = scheduleAttendeeRepository.findMemberIdsByScheduleId(response.getId());
                    
                    // 반복 설정 원본 필드 + 요약 정보 계산
                    Object[] repeatFields = scheduleRepository.findRepeatFieldsByScheduleId(response.getId());
                    String repeatType = null;
                    Integer repeatInterval = null;
                    Integer repeatMonthDay = null;
                    LocalDate repeatEndDate = null;
                    Boolean repeatUseDate = null;
                    List<String> repeatWeekDays = null;
                    String repeatSummary = "반복 없음";
                    if (repeatFields != null && repeatFields.length == 5) {
                        repeatType = (String) repeatFields[0];
                        repeatInterval = (Integer) repeatFields[1];
                        Integer[] repeatDaysArray = (Integer[]) repeatFields[2];
                        repeatMonthDay = (Integer) repeatFields[3];
                        LocalDateTime repeatEndDateDateTime = (LocalDateTime) repeatFields[4];
                        repeatEndDate = repeatEndDateDateTime == null ? null : repeatEndDateDateTime.toLocalDate();
                        
                        // repeatDays를 repeatWeekDays로 변환
                        if (repeatDaysArray != null && repeatDaysArray.length > 0) {
                            repeatWeekDays = convertNumbersToWeekDays(repeatDaysArray);
                        }
                        
                        // repeatMonthDay가 있으면 repeatUseDate = true
                        if (repeatMonthDay != null) {
                            repeatUseDate = true;
                        }
                        
                        ScheduleRepeatRule rule = ScheduleRepeatRule.fromEntityFields(
                                repeatType,
                                repeatInterval,
                                repeatDaysArray,
                                repeatMonthDay,
                                repeatEndDateDateTime
                        );
                        repeatSummary = rule.toSummary();
                    }
                    
                    return ScheduleResponseDto.builder()
                            .id(response.getId())
                            .teamId(response.getTeamId())
                            .teamName(response.getTeamName())
                            .title(response.getTitle())
                            .description(response.getDescription())
                            .startAt(response.getStartAt())
                            .endAt(response.getEndAt())
                            .isAllDay(response.getIsAllDay())
                            .place(response.getPlace())
                            .url(response.getUrl())
                            .createdById(response.getCreatedById())
                            .createdByName(response.getCreatedByName())
                            .createdAt(response.getCreatedAt())
                            .updatedAt(response.getUpdatedAt())
                            .positionIds(positionIds)
                            .positionColors(positionColors)
                            .representativeColorHex(representativeColorHex)
                            .repeatType(repeatType)
                            .repeatInterval(repeatInterval)
                            .repeatEndDate(repeatEndDate)
                            .repeatUseDate(repeatUseDate)
                            .repeatWeekDays(repeatWeekDays)
                            .repeatSummary(repeatSummary)
                            .attendeeMemberIds(attendeeMemberIds)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private ScheduleResponseDto toResponse(Schedule schedule) {
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

        Map<Long, String> positionColors = sortedPositions.stream()
                .collect(Collectors.toMap(
                        sp -> sp.getPosition().getId(),
                        sp -> sp.getPosition().getColorHex(),
                        (existing, replacement) -> existing
                ));

        String representativeColorHex = sortedPositions.isEmpty()
                ? null
                : sortedPositions.get(0).getPosition().getColorHex();

        LocalDate repeatEndDate = schedule.getRepeatEndDate() == null
                ? null
                : schedule.getRepeatEndDate().toLocalDate();

        // repeatDays를 repeatWeekDays로 변환
        List<String> repeatWeekDays = null;
        if (schedule.getRepeatDays() != null && schedule.getRepeatDays().length > 0) {
            repeatWeekDays = convertNumbersToWeekDays(schedule.getRepeatDays());
        }

        // repeatMonthDay가 있으면 repeatUseDate = true
        Boolean repeatUseDate = schedule.getRepeatMonthDay() != null ? true : null;

        // 참석자 팀멤버 ID 목록
        List<Long> attendeeMemberIds = scheduleAttendeeRepository.findMemberIdsByScheduleId(schedule.getId());

        return ScheduleResponseDto.builder()
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
                .positionColors(positionColors)
                .representativeColorHex(representativeColorHex)
                .repeatType(schedule.getRepeatType())
                .repeatInterval(schedule.getRepeatInterval())
                .repeatEndDate(repeatEndDate)
                .repeatUseDate(repeatUseDate)
                .repeatWeekDays(repeatWeekDays)
                .repeatSummary(rule.toSummary())
                .attendeeMemberIds(attendeeMemberIds)
                .build();
    }

    /**
     * 스케줄 변경 알림 전송
     * @param schedule 스케줄
     * @param excludeUserId 알림을 보내지 않을 사용자 ID (생성자/수정자)
     */
    private void sendScheduleChangeNotification(Schedule schedule, Long excludeUserId) {
        if (schedule.getAttendees() == null || schedule.getAttendees().isEmpty()) {
            return;
        }

        Long teamId = schedule.getTeam().getId();
        String scheduleTitle = schedule.getTitle();
        String teamName = schedule.getTeam().getName();

        // 참석자 중 알림을 받을 사용자 목록 수집
        List<String> deviceTokens = new ArrayList<>();
        for (ScheduleAttendee attendee : schedule.getAttendees()) {
            Long userId = attendee.getMember().getUser().getId();
            
            // 생성자/수정자 제외
            if (userId.equals(excludeUserId)) {
                continue;
            }

            // 알림 설정 확인
            boolean shouldNotify = notificationSettingRepository.findByUserIdAndTeamId(userId, teamId)
                    .map(setting -> Boolean.TRUE.equals(setting.getEnableTeamAlarm()) &&
                            Boolean.TRUE.equals(setting.getEnableScheduleChangeNotification()))
                    .orElse(true); // 설정이 없으면 기본값으로 알림 전송

            if (shouldNotify) {
                deviceTokenService.getDeviceTokenByUserId(userId)
                        .ifPresent(deviceTokens::add);
            }
        }

        if (!deviceTokens.isEmpty()) {
            expoNotificationService.sendScheduleChangeNotification(deviceTokens, scheduleTitle, teamName);
        }
    }

}



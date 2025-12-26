package com.example.demo.service;

import com.example.demo.domain.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 스케줄 사전 알림을 자동으로 전송하는 스케줄러
 * 1분마다 실행되어 시작 시간이 가까운 스케줄에 대해 알림을 전송합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulePreNotificationScheduler {

    private final ScheduleRepository scheduleRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final PersonalNotificationSettingRepository personalNotificationSettingRepository;
    private final DeviceTokenService deviceTokenService;
    private final FcmNotificationService fcmNotificationService;

    // 중복 알림 방지를 위한 캐시: (scheduleId, userId, minutesBefore) -> 마지막 알림 시간
    private final Map<String, LocalDateTime> sentNotifications = new ConcurrentHashMap<>();

    /**
     * 스케줄 사전 알림 전송 스케줄러
     * 1분마다 실행되어 시작 시간이 가까운 스케줄을 찾아 알림을 전송합니다.
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional(readOnly = true)
    public void sendSchedulePreNotifications() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourLater = now.plusHours(1);

            // 시작 시간이 1시간 이내인 모든 스케줄 조회 (반복 일정의 부모만)
            List<Schedule> upcomingSchedules = scheduleRepository.findUpcomingSchedulesForNotification(now, oneHourLater);

            if (upcomingSchedules.isEmpty()) {
                return;
            }

            log.debug("스케줄 사전 알림 체크: {}개의 스케줄 발견", upcomingSchedules.size());

            for (Schedule schedule : upcomingSchedules) {
                processSchedulePreNotification(schedule, now);
            }

            // 오래된 캐시 정리 (1시간 이상 지난 알림 기록 삭제)
            cleanupOldNotifications(now);

        } catch (Exception e) {
            log.error("스케줄 사전 알림 전송 중 오류 발생", e);
        }
    }

    /**
     * 개별 스케줄에 대한 사전 알림 처리
     */
    private void processSchedulePreNotification(Schedule schedule, LocalDateTime now) {
        if (schedule.getAttendees() == null || schedule.getAttendees().isEmpty()) {
            return;
        }

        Long teamId = schedule.getTeam().getId();
        String scheduleTitle = schedule.getTitle();
        String teamName = schedule.getTeam().getName();
        LocalDateTime startAt = schedule.getStartAt();

        // 각 참석자에 대해 알림 확인 및 전송
        for (ScheduleAttendee attendee : schedule.getAttendees()) {
            TeamMember member = attendee.getMember();
            User user = member.getUser();
            Long userId = user.getId();

            // 알림 설정 확인
            if (!shouldSendNotification(userId, teamId)) {
                continue;
            }

            // 알림 시간 목록 가져오기
            Integer[] notificationMinutes = getNotificationMinutes(userId, teamId);
            if (notificationMinutes == null || notificationMinutes.length == 0) {
                continue;
            }

            // 각 알림 시간에 대해 체크
            for (Integer minutesBefore : notificationMinutes) {
                LocalDateTime notificationTime = startAt.minusMinutes(minutesBefore);

                // 알림 시간이 현재 시간과 1분 이내인지 확인 (정확도 보정)
                if (notificationTime.isAfter(now.minusMinutes(1)) && notificationTime.isBefore(now.plusMinutes(1))) {
                    // 중복 알림 방지
                    String notificationKey = generateNotificationKey(schedule.getId(), userId, minutesBefore);
                    if (sentNotifications.containsKey(notificationKey)) {
                        continue; // 이미 알림을 보냄
                    }

                    // 알림 전송
                    sendNotificationToUser(userId, scheduleTitle, teamName, minutesBefore);

                    // 알림 기록 저장
                    sentNotifications.put(notificationKey, now);
                    log.info("스케줄 사전 알림 전송: scheduleId={}, userId={}, minutesBefore={}", 
                            schedule.getId(), userId, minutesBefore);
                }
            }
        }
    }

    /**
     * 알림을 전송해야 하는지 확인
     */
    private boolean shouldSendNotification(Long userId, Long teamId) {
        // 팀 알림 설정 확인
        Optional<NotificationSetting> teamSetting = notificationSettingRepository.findByUserIdAndTeamId(userId, teamId);
        if (teamSetting.isPresent()) {
            NotificationSetting setting = teamSetting.get();
            if (Boolean.FALSE.equals(setting.getEnableTeamAlarm())) {
                return false; // 팀 알림이 꺼져 있음
            }
            if (Boolean.FALSE.equals(setting.getEnableSchedulePreNotification())) {
                return false; // 스케줄 사전 알림이 꺼져 있음
            }
        }

        // 개인 알림 설정 확인
        Optional<PersonalNotificationSetting> personalSetting = personalNotificationSettingRepository.findByUserId(userId);
        if (personalSetting.isPresent()) {
            PersonalNotificationSetting setting = personalSetting.get();
            if (Boolean.FALSE.equals(setting.getEnableAllPersonalNotifications())) {
                // 개인 알림이 모두 꺼져 있으면 팀 설정 무시
                return false;
            }
            if (Boolean.FALSE.equals(setting.getEnableSchedulePreNotification())) {
                return false; // 스케줄 사전 알림이 꺼져 있음
            }
        }

        return true; // 기본값: 알림 전송
    }

    /**
     * 알림 시간 목록 가져오기 (팀 설정 우선, 없으면 개인 설정)
     */
    private Integer[] getNotificationMinutes(Long userId, Long teamId) {
        // 팀 알림 설정 확인
        Optional<NotificationSetting> teamSetting = notificationSettingRepository.findByUserIdAndTeamId(userId, teamId);
        if (teamSetting.isPresent()) {
            NotificationSetting setting = teamSetting.get();
            if (Boolean.TRUE.equals(setting.getEnableSchedulePreNotification())) {
                Integer[] minutes = setting.getSchedulePreNotificationMinutes();
                if (minutes != null && minutes.length > 0) {
                    return minutes;
                }
            }
        }

        // 개인 알림 설정 확인
        Optional<PersonalNotificationSetting> personalSetting = personalNotificationSettingRepository.findByUserId(userId);
        if (personalSetting.isPresent()) {
            PersonalNotificationSetting setting = personalSetting.get();
            if (Boolean.TRUE.equals(setting.getEnableSchedulePreNotification())) {
                Integer[] minutes = setting.getSchedulePreNotificationMinutes();
                if (minutes != null && minutes.length > 0) {
                    return minutes;
                }
            }
        }

        return null; // 알림 시간 설정이 없음
    }

    /**
     * 사용자에게 알림 전송
     */
    private void sendNotificationToUser(Long userId, String scheduleTitle, String teamName, int minutesBefore) {
        deviceTokenService.getDeviceTokenByUserId(userId)
                .ifPresent(deviceToken -> {
                    List<String> deviceTokens = Collections.singletonList(deviceToken);
                    fcmNotificationService.sendSchedulePreNotification(deviceTokens, scheduleTitle, teamName, minutesBefore);
                });
    }

    /**
     * 알림 키 생성 (중복 방지용)
     */
    private String generateNotificationKey(Long scheduleId, Long userId, Integer minutesBefore) {
        return String.format("schedule:%d:user:%d:minutes:%d", scheduleId, userId, minutesBefore);
    }

    /**
     * 오래된 알림 기록 정리 (1시간 이상 지난 기록 삭제)
     */
    private void cleanupOldNotifications(LocalDateTime now) {
        LocalDateTime oneHourAgo = now.minusHours(1);
        sentNotifications.entrySet().removeIf(entry -> entry.getValue().isBefore(oneHourAgo));
    }
}


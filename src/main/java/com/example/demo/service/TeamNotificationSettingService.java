package com.example.demo.service;

import com.example.demo.domain.entity.NotificationSetting;
import com.example.demo.domain.entity.Team;
import com.example.demo.domain.entity.User;
import com.example.demo.dto.notification.TeamNotificationSettingRequest;
import com.example.demo.dto.notification.TeamNotificationSettingResponse;
import com.example.demo.repository.NotificationSettingRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamNotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    /**
     * 팀 알림 설정 조회
     */
    @Transactional(readOnly = true)
    public TeamNotificationSettingResponse getTeamNotificationSetting(Long userId, Long teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        NotificationSetting setting = notificationSettingRepository.findByUserAndTeam(user, team)
                .orElseGet(() -> {
                    // 기본값으로 생성
                    NotificationSetting defaultSetting = NotificationSetting.builder()
                            .user(user)
                            .team(team)
                            .build();
                    return notificationSettingRepository.save(defaultSetting);
                });

        return toResponse(setting);
    }

    /**
     * 팀 알림 설정 수정
     */
    @Transactional
    public TeamNotificationSettingResponse updateTeamNotificationSetting(Long userId, Long teamId, TeamNotificationSettingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        NotificationSetting setting = notificationSettingRepository.findByUserAndTeam(user, team)
                .orElseGet(() -> NotificationSetting.builder()
                        .user(user)
                        .team(team)
                        .build());

        // 부분 업데이트
        if (request.getEnableTeamAlarm() != null) {
            setting.setEnableTeamAlarm(request.getEnableTeamAlarm());
        }
        if (request.getEnableScheduleChangeNotification() != null) {
            setting.setEnableScheduleChangeNotification(request.getEnableScheduleChangeNotification());
        }
        if (request.getEnableSchedulePreNotification() != null) {
            setting.setEnableSchedulePreNotification(request.getEnableSchedulePreNotification());
        }
        if (request.getSchedulePreNotificationMinutes() != null) {
            setting.setSchedulePreNotificationMinutes(request.getSchedulePreNotificationMinutes());
        }
        if (request.getEnableTodoChangeNotification() != null) {
            setting.setEnableTodoChangeNotification(request.getEnableTodoChangeNotification());
        }
        if (request.getEnableTodoDeadlineNotification() != null) {
            setting.setEnableTodoDeadlineNotification(request.getEnableTodoDeadlineNotification());
        }
        if (request.getTodoDeadlineNotificationMinutes() != null) {
            setting.setTodoDeadlineNotificationMinutes(request.getTodoDeadlineNotificationMinutes());
        }
        if (request.getEnableTeamMemberNotification() != null) {
            setting.setEnableTeamMemberNotification(request.getEnableTeamMemberNotification());
        }

        setting = notificationSettingRepository.save(setting);
        return toResponse(setting);
    }

    private TeamNotificationSettingResponse toResponse(NotificationSetting setting) {
        return TeamNotificationSettingResponse.builder()
                .id(setting.getId())
                .userId(setting.getUser().getId())
                .teamId(setting.getTeam().getId())
                .teamName(setting.getTeam().getName())
                .enableTeamAlarm(setting.getEnableTeamAlarm())
                .enableScheduleChangeNotification(setting.getEnableScheduleChangeNotification())
                .enableSchedulePreNotification(setting.getEnableSchedulePreNotification())
                .schedulePreNotificationMinutes(setting.getSchedulePreNotificationMinutes())
                .enableTodoChangeNotification(setting.getEnableTodoChangeNotification())
                .enableTodoDeadlineNotification(setting.getEnableTodoDeadlineNotification())
                .todoDeadlineNotificationMinutes(setting.getTodoDeadlineNotificationMinutes())
                .enableTeamMemberNotification(setting.getEnableTeamMemberNotification())
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}



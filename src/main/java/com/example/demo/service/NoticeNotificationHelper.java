package com.example.demo.service;

import com.example.demo.domain.entity.Notice;
import com.example.demo.domain.entity.TeamMember;
import com.example.demo.repository.NotificationSettingRepository;
import com.example.demo.repository.PersonalNotificationSettingRepository;
import com.example.demo.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 공지 알림 전송 헬퍼
 * 공지 생성 시 팀의 모든 멤버에게 알림을 전송합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeNotificationHelper {

    private final ExpoNotificationService expoNotificationService;
    private final DeviceTokenService deviceTokenService;
    private final TeamMemberRepository teamMemberRepository;
    private final PersonalNotificationSettingRepository personalNotificationSettingRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    /**
     * 공지 생성 알림 전송
     * @param notice 생성된 공지
     * @param excludeUserId 알림을 보내지 않을 사용자 ID (작성자)
     */
    public void sendNoticeNotification(Notice notice, Long excludeUserId) {
        Long teamId = notice.getTeam().getId();
        String noticeTitle = notice.getTitle();
        String teamName = notice.getTeam().getName();

        // 팀의 모든 멤버 조회
        List<TeamMember> teamMembers = teamMemberRepository.findByTeamId(teamId);
        if (teamMembers == null || teamMembers.isEmpty()) {
            return;
        }

        // 알림을 받을 사용자 목록 수집
        List<String> deviceTokens = new ArrayList<>();
        for (TeamMember member : teamMembers) {
            Long userId = member.getUser().getId();
            
            // 작성자 제외
            if (userId.equals(excludeUserId)) {
                continue;
            }

            // 팀별 알림 설정 확인 (enableTeamAlarm && enableNoticeNotification)
            boolean teamSettingAllows = notificationSettingRepository.findByUserIdAndTeamId(userId, teamId)
                    .map(setting -> Boolean.TRUE.equals(setting.getEnableTeamAlarm()) &&
                            Boolean.TRUE.equals(setting.getEnableNoticeNotification()))
                    .orElse(true); // 설정이 없으면 기본값으로 알림 전송

            // 개인 알림 설정 확인
            boolean personalSettingAllows = personalNotificationSettingRepository.findByUserId(userId)
                    .map(setting -> Boolean.TRUE.equals(setting.getEnableNoticeNotification()))
                    .orElse(true); // 설정이 없으면 기본값으로 알림 전송

            // 두 설정 모두 활성화된 경우만 알림 전송
            if (teamSettingAllows && personalSettingAllows) {
                deviceTokenService.getDeviceTokenByUserId(userId)
                        .ifPresent(deviceTokens::add);
            }
        }

        if (!deviceTokens.isEmpty()) {
            expoNotificationService.sendNoticeNotification(deviceTokens, noticeTitle, teamName);
            log.info("공지 알림 전송 완료: teamId={}, noticeTitle={}, recipientCount={}", 
                    teamId, noticeTitle, deviceTokens.size());
        }
    }
}


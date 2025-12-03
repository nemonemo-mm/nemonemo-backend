package com.example.demo.dto.notification;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationSettingCreateRequest {
    @NotNull
    private Long teamId;

    // 팀별 알림 전체 ON/OFF (팀 마스터 토글)
    private Boolean enableTeamAlarm;
}


package com.example.demo.dto.notification;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationSettingCreateRequest {
    @NotNull
    private Long teamId;

    private Boolean enableDueAlarm;

    private Integer dueAlarmBeforeMin;

    private Boolean enablePinnedAlarm;
}


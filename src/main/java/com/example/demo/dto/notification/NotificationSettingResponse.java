package com.example.demo.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettingResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long teamId;
    private String teamName;
    private Boolean enableDueAlarm;
    private Integer dueAlarmBeforeMin;
    private Boolean enablePinnedAlarm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}






package com.example.demo.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "개인 알림 설정 응답")
public interface PersonalNotificationSettingResponse {
    @Schema(description = "알림 설정 ID", example = "1")
    Long getId();

    @Schema(description = "사용자 ID", example = "1")
    Long getUserId();

    @Schema(description = "개인 알림 전체 끄기", example = "false")
    Boolean getEnableAllPersonalNotifications();

    @Schema(description = "스케줄 변경 알림", example = "true")
    Boolean getEnableScheduleChangeNotification();

    @Schema(description = "스케줄 미리 알림", example = "true")
    Boolean getEnableSchedulePreNotification();

    @Schema(description = "스케줄 미리 알림 시간 (분 단위 배열)", example = "[10, 30, 60]")
    Integer[] getSchedulePreNotificationMinutes();

    @Schema(description = "투두 변경 알림", example = "true")
    Boolean getEnableTodoChangeNotification();

    @Schema(description = "투두 마감 알림", example = "true")
    Boolean getEnableTodoDeadlineNotification();

    @Schema(description = "투두 마감 알림 시간 (분 단위 배열)", example = "[10, 30, 60]")
    Integer[] getTodoDeadlineNotificationMinutes();

    @Schema(description = "공지 알림", example = "true")
    Boolean getEnableNoticeNotification();

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00.000Z")
    LocalDateTime getCreatedAt();

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00.000Z")
    LocalDateTime getUpdatedAt();
}



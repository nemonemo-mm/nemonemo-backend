package com.example.demo.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "팀 알림 설정 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamNotificationSettingResponse {
    @Schema(description = "알림 설정 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "팀 이름", example = "NemoNemo 팀")
    private String teamName;

    @Schema(description = "팀 전체 알림 끄기", example = "false")
    private Boolean enableTeamAlarm;

    @Schema(description = "스케줄 변경 알림", example = "true")
    private Boolean enableScheduleChangeNotification;

    @Schema(description = "스케줄 미리 알림", example = "true")
    private Boolean enableSchedulePreNotification;

    @Schema(description = "스케줄 미리 알림 시간 (분 단위 배열)", example = "[10, 30, 60]")
    private Integer[] schedulePreNotificationMinutes;

    @Schema(description = "투두 변경 알림", example = "true")
    private Boolean enableTodoChangeNotification;

    @Schema(description = "투두 마감 알림", example = "true")
    private Boolean enableTodoDeadlineNotification;

    @Schema(description = "투두 마감 알림 시간 (분 단위 배열)", example = "[10, 30, 60]")
    private Integer[] todoDeadlineNotificationMinutes;

    @Schema(description = "팀원 알림", example = "true")
    private Boolean enableTeamMemberNotification;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime updatedAt;
}



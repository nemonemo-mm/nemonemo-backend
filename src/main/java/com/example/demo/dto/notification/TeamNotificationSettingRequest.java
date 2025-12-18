package com.example.demo.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "팀 알림 설정 요청")
@Getter
@Setter
public class TeamNotificationSettingRequest {
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
}



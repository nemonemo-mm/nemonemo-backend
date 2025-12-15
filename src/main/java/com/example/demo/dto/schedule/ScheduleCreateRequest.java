package com.example.demo.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleCreateRequest {
    @NotNull
    private Long teamId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 최대 200자까지 입력 가능합니다.")
    @Schema(description = "일정 제목 (필수, 최대 200자)", requiredMode = Schema.RequiredMode.REQUIRED, example = "프로젝트 회의")
    private String title;

    @Schema(description = "일정 설명 (선택, TEXT 타입, 길이 제한 없음)", example = "프로젝트 진행 상황 논의")
    private String description;

    @NotNull
    @Schema(description = "시작일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime startAt;

    @NotNull
    @Schema(description = "종료일시", example = "2024-01-15T12:30:00.000Z")
    private LocalDateTime endAt;

    private Boolean isAllDay;

    private Boolean isPinned;

    @Size(max = 100, message = "장소는 최대 100자까지 입력 가능합니다.")
    @Schema(description = "장소 (선택, 최대 100자)", example = "회의실 A")
    private String place;

    // 개별 일정 알림 오프셋 (end_at 기준 n분 전)
    private Integer reminderOffsetMinutes;
}













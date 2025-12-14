package com.example.demo.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "일정 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponse {
    @Schema(description = "일정 ID", example = "1")
    private Long id;

    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "팀 이름", example = "NemoNemo 팀")
    private String teamName;

    @Schema(description = "제목", example = "프로젝트 회의")
    private String title;

    @Schema(description = "설명", example = "프로젝트 진행 상황 논의")
    private String description;

    @Schema(description = "시작일시")
    private LocalDateTime startAt;

    @Schema(description = "종료일시")
    private LocalDateTime endAt;

    @Schema(description = "종일 일정 여부", example = "false")
    private Boolean isAllDay;

    @Schema(description = "고정 여부", example = "false")
    private Boolean isPinned;

    @Schema(description = "장소", example = "회의실 A")
    private String place;

    @Schema(description = "리마인더 오프셋(분)", example = "30")
    private Integer reminderOffsetMinutes;

    @Schema(description = "생성자 사용자 ID", example = "1")
    private Long createdById;

    @Schema(description = "생성자 이름", example = "홍길동")
    private String createdByName;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;
}













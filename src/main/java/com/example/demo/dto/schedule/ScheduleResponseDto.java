package com.example.demo.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "일정 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponseDto {
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

    @Schema(description = "시작일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime startAt;

    @Schema(description = "종료일시", example = "2024-01-15T12:30:00.000Z")
    private LocalDateTime endAt;

    @Schema(description = "종일 일정 여부", example = "false")
    private Boolean isAllDay;

    @Schema(description = "장소", example = "회의실 A")
    private String place;

    @Schema(description = "URL", example = "https://example.com")
    private String url;

    @Schema(description = "생성자 사용자 ID", example = "1")
    private Long createdById;

    @Schema(description = "생성자 이름", example = "홍길동")
    private String createdByName;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime updatedAt;

    @Schema(description = "포지션 ID 목록 (첫 번째가 대표 포지션)", example = "[1,2]")
    private List<Long> positionIds;

    @Schema(description = "대표 포지션 ID", example = "1")
    private Long representativePositionId;

    @Schema(description = "반복 요약 문자열", example = "2주 간격 · 월, 수")
    private String repeatSummary;

    @Schema(description = "부모 스케줄 ID (반복 기준)", example = "1")
    private Long parentScheduleId;
}


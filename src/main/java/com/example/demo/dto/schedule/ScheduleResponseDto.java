package com.example.demo.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    @Schema(description = "포지션 ID와 색상 매핑 (포지션 ID를 키로, 색상 HEX 값을 값으로)", example = "{\"1\": \"#9BBF9B\", \"2\": \"#FF0000\"}")
    private Map<Long, String> positionColors;

    @Schema(description = "대표 포지션 컬러 HEX 값", example = "#FF0000")
    private String representativeColorHex;

    @Schema(description = "반복 유형 (NONE, DAILY, WEEKLY, MONTHLY, YEARLY)", example = "NONE")
    private String repeatType;

    @Schema(description = "반복 간격 (예: 2일 간격, 2주 간격)", example = "1")
    private Integer repeatInterval;

    @Schema(description = "반복 종료일 (미설정 시 무기한)", example = "2026-01-17")
    private LocalDate repeatEndDate;

    @Schema(description = "월간/연간 반복 시 날짜 사용 여부", example = "true")
    private Boolean repeatUseDate;

    @Schema(description = "반복 요일 배열 (주간 반복 시, \"월\", \"화\", \"수\", \"목\", \"금\", \"토\", \"일\")", example = "[\"월\", \"수\", \"금\"]")
    private List<String> repeatWeekDays;

    @Schema(description = "반복 요약 문자열", example = "2주 간격 · 월, 수")
    private String repeatSummary;

    @Schema(description = "참석자 팀멤버 ID 목록", example = "[10,11]")
    private List<Long> attendeeMemberIds;

    @Schema(description = "스케줄 사전 알림 시간 (분 단위 배열, null이면 사용자 개인 설정 사용)", example = "[10, 30, 60]")
    private Integer[] notificationMinutes;
}


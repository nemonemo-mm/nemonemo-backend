package com.example.demo.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ScheduleCreateRequest {
    @NotNull
    private Long teamId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 20, message = "제목은 최대 20자까지 입력 가능합니다.")
    @Schema(description = "일정 제목 (필수, 최대 20자)", requiredMode = Schema.RequiredMode.REQUIRED, example = "프로젝트 회의")
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

    @Size(max = 100, message = "장소는 최대 100자까지 입력 가능합니다.")
    @Schema(description = "장소 (선택, 최대 100자)", example = "회의실 A")
    private String place;

    @Size(max = 1000, message = "URL은 최대 1000자까지 입력 가능합니다.")
    @Schema(description = "URL (선택, 최대 1000자)", example = "https://example.com")
    private String url;

    // 반복 관련 필드
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

    // 포지션 및 참석자
    @Schema(description = "연결할 포지션 ID 목록 (첫 번째가 대표 포지션, 비어있으면 작성자의 포지션 사용)", example = "[1,2]")
    private List<Long> positionIds;

    @Schema(description = "참석자 팀멤버 ID 목록 (필수)", example = "[10,11]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> attendeeMemberIds;

    @Schema(description = "스케줄 사전 알림 시간 (분 단위 배열, null이면 사용자 개인 설정 사용)", example = "[10, 30, 60]")
    private Integer[] notificationMinutes;
}













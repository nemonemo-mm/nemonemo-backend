package com.example.demo.dto.todo;

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
public class TodoCreateRequest {
    @NotNull
    private Long teamId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 20, message = "제목은 최대 20자까지 입력 가능합니다.")
    @Schema(description = "투두 제목 (필수, 최대 20자)", requiredMode = Schema.RequiredMode.REQUIRED, example = "프로젝트 계획 수립")
    private String title;

    @Schema(description = "투두 설명 (선택, TEXT 타입, 길이 제한 없음)", example = "프로젝트 초기 계획을 수립합니다")
    private String description;

    @NotNull
    @Schema(description = "종료일시 (필수)", example = "2024-01-15T11:30:00.000Z")
    private LocalDateTime endAt;

    @Size(max = 100, message = "장소는 최대 100자까지 입력 가능합니다.")
    @Schema(description = "장소 (선택, 최대 100자)", example = "회의실 A")
    private String place;

    @Size(max = 1000, message = "URL은 최대 1000자까지 입력 가능합니다.")
    @Schema(description = "URL (선택, 최대 1000자)", example = "https://example.com")
    private String url;

    // 담당자 목록 (비어있으면 생성자가 기본 담당자)
    private List<Long> assigneeMemberIds;

    // 포지션 목록
    private List<Long> positionIds;

    // 반복 관련 필드 (스케줄과 동일한 구조)
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
}













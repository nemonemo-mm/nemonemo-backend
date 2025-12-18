package com.example.demo.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ScheduleUpdateRequest {

    @Schema(description = "일정 제목", example = "프로젝트 회의(수정)")
    @Size(max = 200, message = "제목은 최대 200자까지 입력 가능합니다.")
    private String title;

    @Schema(description = "일정 설명", example = "프로젝트 진행 상황 논의 (업데이트)")
    private String description;

    @Schema(description = "시작일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime startAt;

    @Schema(description = "종료일시", example = "2024-01-15T12:30:00.000Z")
    private LocalDateTime endAt;

    private Boolean isAllDay;

    @Size(max = 100, message = "장소는 최대 100자까지 입력 가능합니다.")
    private String place;

    @Size(max = 1000, message = "URL은 최대 1000자까지 입력 가능합니다.")
    @Schema(description = "URL", example = "https://example.com")
    private String url;

    // 반복 관련 필드
    private String repeatType;
    private Integer repeatInterval;
    private List<Integer> repeatDays;
    private Integer repeatMonthDay;
    private LocalDate repeatEndDate;

    // 포지션 및 참석자
    private List<Long> positionIds;
    private List<Long> attendeeMemberIds;
}



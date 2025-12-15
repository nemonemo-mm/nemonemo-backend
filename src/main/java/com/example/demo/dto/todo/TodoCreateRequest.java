package com.example.demo.dto.todo;

import com.example.demo.domain.enums.TodoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TodoCreateRequest {
    @NotNull
    private Long teamId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 최대 200자까지 입력 가능합니다.")
    @Schema(description = "투두 제목 (필수, 최대 200자)", requiredMode = Schema.RequiredMode.REQUIRED, example = "프로젝트 계획 수립")
    private String title;

    @Schema(description = "투두 설명 (선택, TEXT 타입, 길이 제한 없음)", example = "프로젝트 초기 계획을 수립합니다")
    private String description;

    private TodoStatus status;

    @Schema(description = "마감일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime dueAt;

    private Long assigneeMemberId;

    // 개별 투두 알림 오프셋 (due_at 기준 n분 전)
    private Integer reminderOffsetMinutes;
}













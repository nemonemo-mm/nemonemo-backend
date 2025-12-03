package com.example.demo.dto.todo;

import com.example.demo.domain.enums.TodoStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TodoCreateRequest {
    @NotNull
    private Long teamId;

    @NotBlank
    private String title;

    private String description;

    private TodoStatus status;

    private LocalDateTime dueAt;

    private Long assigneeMemberId;

    // 개별 투두 알림 오프셋 (due_at 기준 n분 전)
    private Integer reminderOffsetMinutes;
}













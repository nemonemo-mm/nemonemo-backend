package com.example.demo.dto.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleCreateRequest {
    @NotNull
    private Long teamId;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    private Boolean isAllDay;

    private Boolean isPinned;
}








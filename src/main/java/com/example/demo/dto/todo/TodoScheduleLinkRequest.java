package com.example.demo.dto.todo;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoScheduleLinkRequest {
    @NotNull
    private Long todoId;

    @NotNull
    private Long scheduleId;
}







package com.example.demo.dto.todo;

import com.example.demo.domain.enums.TodoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoStatusUpdateRequest {

    @NotNull(message = "상태는 필수입니다.")
    @Schema(description = "투두 상태 (TODO, DONE)", example = "DONE", required = true)
    private TodoStatus status;
}


package com.example.demo.dto.notice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeCreateRequest {
    @NotNull
    private Long teamId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;
}



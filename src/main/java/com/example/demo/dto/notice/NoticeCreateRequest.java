package com.example.demo.dto.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeCreateRequest {
    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "공지사항 내용 (필수, TEXT 타입, 길이 제한 없음)", requiredMode = Schema.RequiredMode.REQUIRED, example = "다음 주 월요일부터 프로젝트 일정이 변경됩니다.")
    private String content;
}



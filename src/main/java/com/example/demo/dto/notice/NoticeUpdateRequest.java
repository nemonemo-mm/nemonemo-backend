package com.example.demo.dto.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeUpdateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 최대 200자까지 입력 가능합니다.")
    @Schema(description = "공지사항 제목 (필수, 최대 200자)", requiredMode = Schema.RequiredMode.REQUIRED, example = "프로젝트 일정 변경 안내 (수정)")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "공지사항 내용 (필수, TEXT 타입, 길이 제한 없음)", requiredMode = Schema.RequiredMode.REQUIRED, example = "다음 주 월요일부터 프로젝트 일정이 변경됩니다. (수정된 내용)")
    private String content;
}


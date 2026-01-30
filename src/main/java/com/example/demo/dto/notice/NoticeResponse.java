package com.example.demo.dto.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "공지사항 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeResponse {
    @Schema(description = "공지사항 ID", example = "1")
    private Long id;

    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "팀 이름", example = "NemoNemo 팀")
    private String teamName;

    @Schema(description = "내용", example = "다음 주 월요일부터 프로젝트 일정이 변경됩니다.")
    private String content;

    @Schema(description = "작성자 사용자 ID", example = "1")
    private Long authorId;

    @Schema(description = "작성자 이름", example = "홍길동")
    private String authorName;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime updatedAt;
}


















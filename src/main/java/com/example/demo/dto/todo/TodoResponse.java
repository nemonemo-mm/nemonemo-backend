package com.example.demo.dto.todo;

import com.example.demo.domain.enums.TodoStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "투두 정보")
public interface TodoResponse {
    @Schema(description = "투두 ID", example = "1")
    Long getId();

    @Schema(description = "팀 ID", example = "1")
    Long getTeamId();

    @Schema(description = "팀 이름", example = "NemoNemo 팀")
    String getTeamName();

    @Schema(description = "제목", example = "프로젝트 계획 수립")
    String getTitle();

    @Schema(description = "설명", example = "프로젝트 초기 계획을 수립합니다")
    String getDescription();

    @Schema(description = "상태", example = "TODO")
    TodoStatus getStatus();

    @Schema(description = "종료일시", example = "2024-01-15T11:30:00.000Z")
    LocalDateTime getEndAt();

    @Schema(description = "장소", example = "회의실 A")
    String getPlace();

    @Schema(description = "URL", example = "https://example.com")
    String getUrl();

    @Schema(description = "생성자 사용자 ID", example = "1")
    Long getCreatedById();

    @Schema(description = "생성자 이름", example = "홍길동")
    String getCreatedByName();

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00.000Z")
    LocalDateTime getCreatedAt();

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00.000Z")
    LocalDateTime getUpdatedAt();
}

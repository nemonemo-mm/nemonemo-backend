package com.example.demo.dto.todo;

import com.example.demo.domain.enums.TodoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "투두 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoResponse {
    @Schema(description = "투두 ID", example = "1")
    private Long id;

    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "팀 이름", example = "NemoNemo 팀")
    private String teamName;

    @Schema(description = "제목", example = "프로젝트 계획 수립")
    private String title;

    @Schema(description = "설명", example = "프로젝트 초기 계획을 수립합니다")
    private String description;

    @Schema(description = "상태", example = "TODO")
    private TodoStatus status;

    @Schema(description = "마감일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime dueAt;

    @Schema(description = "리마인더 오프셋(분)", example = "30")
    private Integer reminderOffsetMinutes;

    @Schema(description = "생성자 사용자 ID", example = "1")
    private Long createdById;

    @Schema(description = "생성자 이름", example = "홍길동")
    private String createdByName;

    @Schema(description = "담당자 팀멤버 ID", example = "2")
    private Long assigneeMemberId;

    @Schema(description = "담당자 사용자 이름", example = "홍길동")
    private String assigneeMemberUserName;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime updatedAt;
}













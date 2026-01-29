package com.example.demo.dto.todo;

import com.example.demo.domain.enums.TodoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "투두 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoResponseDto {
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

    @Schema(description = "종료일시", example = "2024-01-15T11:30:00.000Z")
    private LocalDateTime endAt;

    @Schema(description = "장소", example = "회의실 A")
    private String place;

    @Schema(description = "URL", example = "https://example.com")
    private String url;

    @Schema(description = "생성자 사용자 ID", example = "1")
    private Long createdById;

    @Schema(description = "생성자 이름", example = "홍길동")
    private String createdByName;

    @Schema(description = "담당자 팀멤버 ID", example = "2")
    private Long assigneeMemberId;

    @Schema(description = "담당자 사용자 이름", example = "홍길동")
    private String assigneeMemberUserName;

    @Schema(description = "담당자 목록", example = "[{\"memberId\":2,\"userName\":\"홍길동\"}]")
    private List<TodoAssigneeDto> assignees;

    @Schema(description = "포지션 ID 목록", example = "[1,2]")
    private List<Long> positionIds;

    @Schema(description = "대표 포지션 컬러 HEX 값", example = "#FF0000")
    private String representativeColorHex;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime updatedAt;
}


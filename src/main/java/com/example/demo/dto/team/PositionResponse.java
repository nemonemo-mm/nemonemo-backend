package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "포지션 정보")
public interface PositionResponse {
    @Schema(description = "포지션 ID", example = "1")
    Long getPositionId();

    @Schema(description = "팀 ID", example = "1")
    Long getTeamId();

    @Schema(description = "포지션 이름", example = "Design")
    String getPositionName();

    @Schema(description = "포지션 색상 (HEX)", example = "#FFAA00")
    String getColorHex();

    @Schema(description = "기본 포지션 여부", example = "false")
    Boolean getIsDefault();

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00.000Z")
    LocalDateTime getCreatedAt();

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00.000Z")
    LocalDateTime getUpdatedAt();
}




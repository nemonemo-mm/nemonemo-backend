package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "포지션 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionResponse {
    @Schema(description = "포지션 ID", example = "1")
    private Long id;

    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "포지션 이름", example = "Design")
    private String name;

    @Schema(description = "포지션 색상 (HEX)", example = "#FFAA00")
    private String colorHex;

    @Schema(description = "기본 포지션 여부", example = "false")
    private Boolean isDefault;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;
}




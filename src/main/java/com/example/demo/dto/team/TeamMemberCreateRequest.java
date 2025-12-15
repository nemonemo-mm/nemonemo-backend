package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamMemberCreateRequest {
    @Schema(description = "포지션 ID (선택)", example = "2")
    private Long positionId;
}


















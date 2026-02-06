package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "팀 목록 항목 (사이드바용)")
public interface TeamListItemResponse {
    @Schema(description = "팀 ID", example = "1")
    Long getTeamId();

    @Schema(description = "팀 이름", example = "NemoNemo 팀")
    String getTeamName();

    @Schema(description = "사용자의 포지션 이름 (추후 positionName으로 변경 예정)", example = "Design")
    String getDescription(); // 추후 positionName으로 변경 예정

    @Schema(description = "팀 이미지 URL", example = "https://firebasestorage.googleapis.com/v0/b/your-project.appspot.com/o/teams%2F...")
    String getTeamImageUrl();
}


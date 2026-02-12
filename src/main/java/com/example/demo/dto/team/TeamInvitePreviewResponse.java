package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "인바이트 코드로 조회한 팀 정보 (가입 전 미리보기)")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamInvitePreviewResponse {
    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "팀 이름", example = "NemoNemo 팀")
    private String teamName;

    @Schema(description = "팀장 이름", example = "홍길동")
    private String ownerName;

    @Schema(description = "팀 소개", example = "우리팀 소개입니다")
    private String description;

    @Schema(description = "팀 이미지 URL", example = "https://firebasestorage.googleapis.com/v0/b/your-project.appspot.com/o/teams%2F...")
    private String teamImageUrl;

    @Schema(description = "포지션 목록")
    private List<PositionResponse> positions;
}

















package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "팀 멤버 정보")
public interface TeamMemberResponse {
    @Schema(description = "팀 멤버 ID", example = "1")
    Long getMemberId();

    @Schema(description = "팀 ID", example = "1")
    Long getTeamId();

    @Schema(description = "팀 이름", example = "NemoNemo 팀")
    String getTeamName();

    @Schema(description = "사용자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    String getUserImageUrl();

    @Schema(description = "사용자 ID", example = "1")
    Long getUserId();

    @Schema(description = "사용자 이름", example = "홍길동")
    String getUserName();

    @Schema(description = "사용자 이메일", example = "user@example.com")
    String getUserEmail();

    @Schema(description = "포지션 ID", example = "2")
    Long getPositionId();

    @Schema(description = "포지션 이름", example = "Design")
    String getPositionName();

    @Schema(description = "포지션 색상 (HEX)", example = "#FFAA00")
    String getPositionColor();

    @Schema(description = "팀장 여부", example = "false")
    Boolean getIsOwner();

    @Schema(description = "가입일시", example = "2024-01-15T10:30:00.000Z")
    LocalDateTime getJoinedAt();
}


















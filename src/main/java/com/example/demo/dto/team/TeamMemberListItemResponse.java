package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "팀 멤버 목록 항목")
public interface TeamMemberListItemResponse {
    @Schema(description = "팀 멤버 ID", example = "1")
    Long getMemberId();

    @Schema(description = "사용자 ID", example = "1")
    Long getUserId();

    @Schema(description = "표시 이름 (사용자 이름)", example = "홍길동")
    String getDisplayName(); // 사용자 이름

    @Schema(description = "포지션 ID", example = "1")
    Long getPositionId();

    @Schema(description = "포지션 이름", example = "Design")
    String getPositionName();

    @Schema(description = "사용자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    String getUserImageUrl();
}




package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "팀 멤버 목록 항목")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberListItemResponse {
    @Schema(description = "팀 멤버 ID", example = "1")
    private Long id;

    @Schema(description = "표시 이름 (닉네임 혹은 사용자 이름)", example = "팀원1")
    private String displayName; // 닉네임 혹은 사용자 이름

    @Schema(description = "포지션 이름", example = "Design")
    private String positionName;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String imageUrl;
}




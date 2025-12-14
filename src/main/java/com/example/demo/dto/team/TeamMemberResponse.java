package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "팀 멤버 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberResponse {
    @Schema(description = "팀 멤버 ID", example = "1")
    private Long id;

    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "팀 이름", example = "NemoNemo 팀")
    private String teamName;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String imageUrl;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String userName;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String userEmail;

    @Schema(description = "닉네임", example = "팀원1")
    private String nickname;

    @Schema(description = "포지션 ID", example = "2")
    private Long positionId;

    @Schema(description = "포지션 이름", example = "Design")
    private String positionName;

    @Schema(description = "포지션 색상 (HEX)", example = "#FFAA00")
    private String positionColor;

    @Schema(description = "팀장 여부", example = "false")
    private Boolean isOwner;

    @Schema(description = "가입일시")
    private LocalDateTime joinedAt;
}


















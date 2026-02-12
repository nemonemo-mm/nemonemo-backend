package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "팀원 목록 조회 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberListResponse {
    @Schema(description = "팀 이름", example = "NemoNemo 팀")
    private String teamName;

    @Schema(description = "팀장 정보")
    private OwnerInfo ownerInfo;

    @Schema(description = "팀원 목록")
    private List<TeamMemberListItemResponse> members;

    @Schema(description = "팀장 정보")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OwnerInfo {
        @Schema(description = "팀장 사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "팀장 이름", example = "홍길동")
        private String ownerName;

        @Schema(description = "팀장 프로필 이미지 URL", example = "https://firebasestorage.googleapis.com/v0/b/your-project.appspot.com/o/users%2F...")
        private String ownerImageUrl;
    }
}

















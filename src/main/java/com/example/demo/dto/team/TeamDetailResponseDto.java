package com.example.demo.dto.team;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "팀 상세 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDetailResponseDto {
    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "팀 이름", example = "NemoNemo 팀")
    private String teamName;

    @Schema(description = "초대 코드 (팀장만 조회 가능)", example = "ABC123XY")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String inviteCode;

    @Schema(description = "팀장 사용자 ID", example = "1")
    private Long ownerId;

    @Schema(description = "팀장 이름", example = "홍길동")
    private String ownerName;

    @Schema(description = "현재 사용자가 팀장인지 여부", example = "true")
    private Boolean isOwner;

    @Schema(description = "팀 소개", example = "우리팀 소개입니다")
    private String description;

    @Schema(description = "팀 이미지 URL", example = "https://firebasestorage.googleapis.com/v0/b/your-project.appspot.com/o/teams%2F...")
    private String teamImageUrl;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00.000Z")
    private LocalDateTime updatedAt;
}














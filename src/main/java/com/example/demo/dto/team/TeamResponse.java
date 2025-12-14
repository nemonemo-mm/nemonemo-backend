package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "팀 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {
    @Schema(description = "팀 ID", example = "1")
    private Long id;

    @Schema(description = "팀 이름", example = "NemoNemo 팀")
    private String name;

    @Schema(description = "초대 코드", example = "ABC123XY")
    private String inviteCode;

    @Schema(description = "팀장 사용자 ID", example = "1")
    private Long ownerId;

    @Schema(description = "팀장 이름", example = "홍길동")
    private String ownerName;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;
}














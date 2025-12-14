package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "팀 탈퇴 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamLeaveResponse {
    @Schema(description = "팀 ID", example = "1")
    private Long teamId;
    
    @Schema(description = "탈퇴한 멤버 ID", example = "1")
    private Long memberId;
    
    @Schema(description = "탈퇴한 사용자 ID", example = "1")
    private Long userId;
}

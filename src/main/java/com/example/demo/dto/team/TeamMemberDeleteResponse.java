package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "팀원 삭제 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberDeleteResponse {
    @Schema(description = "팀 ID", example = "1")
    private Long teamId;
    
    @Schema(description = "팀원 ID", example = "1")
    private Long memberId;
    
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
}

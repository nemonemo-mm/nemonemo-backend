package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "팀 삭제 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDeleteResponse {
    @Schema(description = "팀 ID", example = "1")
    private Long teamId;
}






































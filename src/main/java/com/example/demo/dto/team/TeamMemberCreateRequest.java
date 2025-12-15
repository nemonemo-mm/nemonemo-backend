package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamMemberCreateRequest {
    @Size(max = 10, message = "닉네임은 최대 10자까지 입력 가능합니다.")
    @Schema(description = "닉네임 (선택, 최대 10자)", example = "팀원1")
    private String nickname;
    
    @Schema(description = "포지션 ID (선택)", example = "2")
    private Long positionId;
}


















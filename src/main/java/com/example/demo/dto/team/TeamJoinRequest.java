package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamJoinRequest {
    @Schema(description = "초대 코드 (필수)", requiredMode = Schema.RequiredMode.REQUIRED, example = "ABC123XY")
    @NotBlank(message = "초대 코드는 필수입니다.")
    private String inviteCode;
    
    @Schema(description = "포지션 ID (선택, 미선택 시 기본 포지션 MEMBER로 자동 할당)", example = "2")
    private Long positionId;
}


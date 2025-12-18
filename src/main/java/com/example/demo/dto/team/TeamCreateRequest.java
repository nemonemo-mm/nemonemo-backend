package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamCreateRequest {
    @Schema(description = "팀 이름 (필수, 최대 10자)", requiredMode = Schema.RequiredMode.REQUIRED, example = "NemoNemo 팀")
    @NotBlank(message = "팀 이름은 필수입니다.")
    @Size(max = 10, message = "팀 이름은 최대 10자까지 입력 가능합니다.")
    private String teamName;
    
    @Schema(description = "팀 소개 (선택, 최대 20자)", example = "우리팀 소개입니다")
    @Size(max = 20, message = "팀 소개는 최대 20자까지 입력 가능합니다.")
    private String description;

    @Schema(description = "팀 생성자의 포지션 ID (선택, 미선택 시 기본 포지션 MEMBER로 자동 할당)", example = "1")
    private Long ownerPositionId;
}


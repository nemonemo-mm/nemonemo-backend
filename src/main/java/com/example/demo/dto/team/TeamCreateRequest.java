package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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

    @Schema(description = "팀 생성 시 함께 생성할 포지션 목록 (선택, 최대 6개, 기본 포지션 MEMBER는 자동 생성됨)", 
            example = "[{\"positionName\": \"Design\", \"colorHex\": \"#FFAA00\"}, {\"positionName\": \"Developer\", \"colorHex\": \"#00AAFF\"}]")
    @Valid
    private List<PositionCreateRequest> positions;

    @Schema(description = "팀 생성자의 포지션 이름 (선택, 미선택 시 기본 포지션 MEMBER로 자동 할당). positions에 포함된 포지션 이름이거나 'MEMBER'여야 함", example = "Design")
    private String ownerPositionName;
}


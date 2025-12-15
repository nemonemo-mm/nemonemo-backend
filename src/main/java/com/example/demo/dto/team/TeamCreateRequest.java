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
    private String name;
    
    @Schema(description = "팀 소개 (선택, TEXT 타입, 길이 제한 없음)", example = "우리팀 소개입니다")
    private String description;
    
    @Schema(description = "포지션 목록 (선택, 최대 6개, MEMBER는 자동생성)", example = "[{\"name\": \"Design\", \"colorHex\": \"#FFAA00\"}]")
    @Valid
    private List<PositionCreateRequest> positions;
}


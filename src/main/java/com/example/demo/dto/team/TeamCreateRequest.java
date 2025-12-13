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
    @Schema(description = "팀 이름 (필수, 최대 10자)", requiredMode = Schema.RequiredMode.REQUIRED, example = "nemonemo")
    @NotBlank(message = "팀 이름은 필수입니다.")
    @Size(max = 10, message = "팀 이름은 최대 10자까지 입력 가능합니다.")
    private String name;
    
    @Schema(description = "프로필 이미지 URL (선택, 최대 255자)", example = "https://example.com/image.jpg")
    @Size(max = 255, message = "이미지 URL은 최대 255자까지 입력 가능합니다.")
    private String imageUrl;
    
    @Schema(description = "팀 소개 (선택, 최대 50자)", example = "우리팀 소개")
    @Size(max = 50, message = "팀 소개는 최대 50자까지 입력 가능합니다.")
    private String description;
    
    @Schema(description = "포지션 목록 (선택, 최대 6개, MEMBER는 자동생성)", example = "[{\"name\": \"Design\", \"colorHex\": \"#FFAA00\"}]")
    @Valid
    private List<PositionCreateRequest> positions;
}


package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionCreateRequest {
    @Schema(description = "포지션 이름 (필수, 최대 10자)", requiredMode = Schema.RequiredMode.REQUIRED, example = "Design")
    @NotBlank(message = "포지션 이름은 필수입니다.")
    @Size(max = 10, message = "포지션 이름은 최대 10자까지 입력 가능합니다.")
    private String positionName;
    
    @Schema(description = "포지션 색상 (선택)", example = "#FFAA00")
    @Size(max = 9, message = "색상 코드는 최대 9자까지 입력 가능합니다.")
    private String colorHex;
}


package com.example.demo.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamUpdateRequest {
    @Schema(description = "팀 이름 (선택, 최대 100자)", example = "캡스톤 팀 B")
    @Size(max = 100, message = "팀 이름은 최대 100자까지 입력 가능합니다.")
    private String name;
    
    @Schema(description = "팀 소개 (선택, TEXT 타입, 길이 제한 없음)", example = "수정된 팀 소개입니다")
    private String description;
}


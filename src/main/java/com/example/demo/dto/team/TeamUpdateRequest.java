package com.example.demo.dto.team;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamUpdateRequest {
    @Schema(description = "팀 이름 (선택, 최대 10자)", example = "캡스톤 팀 B")
    @Size(max = 10, message = "팀 이름은 최대 10자까지 입력 가능합니다.")
    private String teamName;
    
    @Schema(description = "팀 소개 (선택, TEXT 타입, 길이 제한 없음)", example = "수정된 팀 소개입니다")
    private String description;
    
    @Schema(description = "팀 이미지 URL 삭제 플래그 (선택). true면 이미지 삭제, false 또는 null이면 이미지 유지", 
            example = "true")
    @JsonInclude(JsonInclude.Include.ALWAYS)  // null 필드도 포함하여 명시적 null 처리 가능
    private Boolean deleteImageUrl;
}


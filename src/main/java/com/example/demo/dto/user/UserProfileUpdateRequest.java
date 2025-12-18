package com.example.demo.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequest {
    
    @Schema(description = "사용자 이름", example = "홍길동")
    @NotBlank(message = "사용자 이름은 필수입니다.")
    @Size(max = 10, message = "사용자 이름은 최대 10자까지 입력 가능합니다.")
    private String userName;
}


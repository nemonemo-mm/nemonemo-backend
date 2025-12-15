package com.example.demo.dto.user;

import com.example.demo.domain.enums.AuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {
    @NotBlank
    @Email
    private String email;

    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 10, message = "이름은 최대 10자까지 입력 가능합니다.")
    @Schema(description = "사용자 이름 (필수, 최대 10자)", requiredMode = Schema.RequiredMode.REQUIRED, example = "홍길동")
    private String name;

    private AuthProvider provider;

    private String providerId;
}













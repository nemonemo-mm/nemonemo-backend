package com.example.demo.dto.user;

import com.example.demo.domain.enums.AuthProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {
    @NotBlank
    @Email
    private String email;

    private String password;

    @NotBlank
    private String name;

    private AuthProvider provider;

    private String providerId;
}






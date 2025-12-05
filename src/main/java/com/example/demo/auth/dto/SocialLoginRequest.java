package com.example.demo.auth.dto;

import com.example.demo.domain.enums.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginRequest {

    @NotNull
    private AuthProvider provider;

    @NotBlank
    private String idToken;

    private String name;

    private String imageUrl;
}



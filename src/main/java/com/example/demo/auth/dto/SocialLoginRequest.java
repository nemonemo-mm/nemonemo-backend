package com.example.demo.auth.dto;

import com.example.demo.domain.enums.AuthProvider;
import com.example.demo.domain.enums.ClientType;
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

    /**
     * 클라이언트 플랫폼 타입 (iOS, Android, Web)
     */
    private ClientType clientType;

    private String name;

    private String imageUrl;
}



package com.example.demo.auth.dto;

import com.example.demo.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthTokensResponse {
    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;
    private UserResponse user;
}



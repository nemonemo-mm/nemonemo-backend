package com.example.demo.auth.controller;

import com.example.demo.auth.dto.AuthTokensResponse;
import com.example.demo.auth.dto.SocialLoginRequest;
import com.example.demo.auth.service.SocialAuthService;
import com.example.demo.domain.enums.AuthProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "인증", description = "소셜 로그인 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SocialAuthController {

    private final SocialAuthService socialAuthService;

    @Operation(summary = "소셜 로그인", description = "Google ID 토큰을 사용하여 소셜 로그인을 수행합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공", 
            content = @Content(schema = @Schema(implementation = AuthTokensResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패 또는 지원하지 않는 provider)"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/social/login")
    public ResponseEntity<Map<String, Object>> socialLogin(@Valid @RequestBody SocialLoginRequest request) {

        if (request.getProvider() != AuthProvider.GOOGLE) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "INVALID_REQUEST");
            response.put("message", "현재는 GOOGLE provider만 지원합니다.");
            response.put("data", null);
            response.put("meta", null);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            AuthTokensResponse tokens = socialAuthService.loginWithGoogle(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", "SUCCESS");
            response.put("message", null);
            response.put("data", tokens);
            response.put("meta", null);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 토큰 검증 실패
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "AUTH_INVALID_TOKEN");
            response.put("message", "유효하지 않은 소셜 토큰입니다.");
            response.put("data", null);
            response.put("meta", null);
            return ResponseEntity.status(401).body(response);
        } catch (GeneralSecurityException | IOException e) {
            // Google 검증 서버 오류 등
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "INTERNAL_SERVER_ERROR");
            response.put("message", "소셜 토큰 검증 중 오류가 발생했습니다.");
            response.put("data", null);
            response.put("meta", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}


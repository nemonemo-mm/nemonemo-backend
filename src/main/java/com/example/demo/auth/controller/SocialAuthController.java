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

    @Operation(summary = "소셜 로그인", description = "Google ID 토큰을 사용하여 소셜 로그인을 수행합니다. " +
            "기존 사용자는 자동으로 로그인되며, 신규 사용자는 회원가입 후 로그인됩니다. " +
            "신규 회원가입 시에는 name 필드가 필수입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공", 
            content = @Content(schema = @Schema(implementation = AuthTokensResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 지원하지 않는 provider, 또는 사용자 이름 누락)", 
            content = @Content(schema = @Schema(example = "{\"success\":false,\"code\":\"AUTH_MISSING_NAME\",\"message\":\"사용자 이름이 필요합니다.\",\"data\":null,\"meta\":null}"))),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰", 
            content = @Content(schema = @Schema(example = "{\"success\":false,\"code\":\"AUTH_INVALID_TOKEN\",\"message\":\"유효하지 않은 소셜 토큰입니다.\",\"data\":null,\"meta\":null}"))),
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
            // 에러 코드에 따라 다른 응답 반환
            String errorCode = e.getMessage() != null && e.getMessage().contains(":") 
                ? e.getMessage().split(":")[0] 
                : "INVALID_REQUEST";
            String errorMessage = e.getMessage() != null && e.getMessage().contains(":") 
                ? e.getMessage().split(":", 2)[1].trim() 
                : e.getMessage();

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", errorCode);
            response.put("message", errorMessage);
            response.put("data", null);
            response.put("meta", null);
            
            // AUTH_MISSING_NAME은 400, AUTH_INVALID_TOKEN은 401
            if ("AUTH_MISSING_NAME".equals(errorCode)) {
                return ResponseEntity.badRequest().body(response);
            } else if ("AUTH_INVALID_TOKEN".equals(errorCode)) {
                return ResponseEntity.status(401).body(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
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


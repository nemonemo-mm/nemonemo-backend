package com.example.demo.auth.controller;

import com.example.demo.auth.dto.AuthTokensResponse;
import com.example.demo.auth.dto.SocialLoginRequest;
import com.example.demo.auth.service.SocialAuthService;
import com.example.demo.domain.enums.AuthProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestHeader;
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

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다. " +
            "Refresh Token은 Authorization 헤더에 Bearer 형식으로 전달해야 합니다. " +
            "Refresh Token 로테이션 정책: " +
            "1. Refresh Token은 1회성 토큰이며, 재발급 시 기존 토큰은 즉시 폐기됩니다. " +
            "2. 동시 요청이 여러 번 들어오는 경우, 가장 먼저 처리된 요청만 유효합니다. " +
            "3. 이후 요청들은 이미 폐기된 Refresh Token을 사용하므로 INVALID_REFRESH_TOKEN을 반환합니다. " +
            "자동 로그인 기능에 사용됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 재발급 성공 (새로운 Access Token과 Refresh Token 반환)", 
            content = @Content(schema = @Schema(example = "{\"success\":true,\"code\":\"SUCCESS\",\"message\":null,\"data\":{\"accessToken\":\"new-jwt-access-token\",\"refreshToken\":\"new-refresh-token\",\"isNewUser\":false,\"user\":null},\"meta\":null}"))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (Authorization 헤더 누락 또는 형식 오류)", 
            content = @Content(schema = @Schema(example = "{\"success\":false,\"code\":\"INVALID_REQUEST\",\"message\":\"Authorization 헤더에 Bearer 토큰이 필요합니다.\",\"data\":null,\"meta\":null}"))),
        @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 토큰, 또는 이미 사용된 토큰", 
            content = @Content(schema = @Schema(example = "{\"success\":false,\"code\":\"INVALID_REFRESH_TOKEN\",\"message\":\"유효하지 않은 리프레시 토큰입니다.\",\"data\":null,\"meta\":null}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            // Authorization 헤더에서 Bearer 토큰 추출
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("code", "INVALID_REQUEST");
                response.put("message", "Authorization 헤더에 Bearer 토큰이 필요합니다.");
                response.put("data", null);
                response.put("meta", null);
                return ResponseEntity.badRequest().body(response);
            }
            
            String refreshToken = authorizationHeader.substring(7); // "Bearer " 제거
            AuthTokensResponse tokens = socialAuthService.refreshAccessToken(refreshToken);

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
            
            // AUTH_TOKEN_EXPIRED, INVALID_REFRESH_TOKEN은 401
            if ("AUTH_TOKEN_EXPIRED".equals(errorCode) || "INVALID_REFRESH_TOKEN".equals(errorCode)) {
                return ResponseEntity.status(401).body(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            // 예상치 못한 오류
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "INTERNAL_SERVER_ERROR");
            response.put("message", "토큰 재발급 중 오류가 발생했습니다.");
            response.put("data", null);
            response.put("meta", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}


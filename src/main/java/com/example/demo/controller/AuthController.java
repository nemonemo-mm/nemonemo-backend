package com.example.demo.controller;

import com.example.demo.dto.auth.AuthTokensResponse;
import com.example.demo.dto.auth.SocialLoginRequest;
import com.example.demo.service.SocialAuthService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "인증", description = "소셜 로그인 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SocialAuthService socialAuthService;

    @Operation(summary = "소셜 로그인", description = "Firebase Authentication SDK에서 발급받은 ID Token을 사용하여 소셜 로그인을 수행합니다. " +
            "기존 사용자는 자동으로 로그인되며, 신규 사용자는 회원가입 후 로그인됩니다. " +
            "신규 회원가입 시에는 name 필드가 필수입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공", 
            content = @Content(schema = @Schema(implementation = AuthTokensResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 지원하지 않는 provider, 또는 사용자 이름 누락)", 
            content = @Content(schema = @Schema(example = "{\"success\":false,\"code\":\"AUTH_MISSING_NAME\",\"message\":\"사용자 이름이 필요합니다.\",\"data\":null,\"meta\":null}"))),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Firebase ID 토큰", 
            content = @Content(schema = @Schema(example = "{\"success\":false,\"code\":\"AUTH_INVALID_TOKEN\",\"message\":\"유효하지 않은 Firebase ID 토큰입니다.\",\"data\":null,\"meta\":null}"))),
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
            return handleIllegalArgumentException(e);
        } catch (com.google.firebase.auth.FirebaseAuthException e) {
            // Firebase 인증 오류
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "AUTH_INVALID_TOKEN");
            response.put("message", "유효하지 않은 Firebase ID 토큰입니다.");
            response.put("data", null);
            response.put("meta", null);
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            // 예상치 못한 오류
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("code", "INTERNAL_SERVER_ERROR");
            response.put("message", "Firebase 토큰 검증 중 오류가 발생했습니다.");
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
            content = @Content(schema = @Schema(implementation = AuthTokensResponse.class))),
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
            return handleIllegalArgumentException(e);
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
    
    /**
     * IllegalArgumentException 처리 (권한, 리소스 없음, 인증 에러 등을 구분)
     */
    private ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = e.getMessage();
        
        // 에러 코드가 포함된 경우 (예: "FORBIDDEN: ...", "AUTH_INVALID_TOKEN: ...")
        if (message != null && message.contains(":")) {
            String errorCode = message.split(":")[0].trim();
            String cleanMessage = message.split(":", 2)[1].trim();
            
            HttpStatus status;
            // AUTH 관련 에러는 401
            if ("AUTH_INVALID_TOKEN".equals(errorCode) || "INVALID_REFRESH_TOKEN".equals(errorCode) 
                    || "AUTH_TOKEN_EXPIRED".equals(errorCode)) {
                status = HttpStatus.UNAUTHORIZED;
            } else if ("FORBIDDEN".equals(errorCode)) {
                status = HttpStatus.FORBIDDEN;
            } else if ("TEAM_NOT_FOUND".equals(errorCode) || "TEAM_MEMBER_NOT_FOUND".equals(errorCode) 
                    || "POSITION_NOT_FOUND".equals(errorCode) || "NOT_FOUND".equals(errorCode)) {
                status = HttpStatus.NOT_FOUND;
            } else {
                // AUTH_MISSING_NAME 등은 400
                status = HttpStatus.BAD_REQUEST;
            }
            
            return createErrorResponseWithCode(errorCode, cleanMessage, status);
        }
        
        // 에러 코드가 없는 경우 메시지로 판단
        String code = "INVALID_REQUEST";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        if (message != null) {
            if (message.contains("권한") || message.contains("FORBIDDEN") || message.contains("멤버만")) {
                code = "FORBIDDEN";
                status = HttpStatus.FORBIDDEN;
            } else if (message.contains("찾을 수 없습니다") || message.contains("NOT_FOUND")) {
                code = "NOT_FOUND";
                status = HttpStatus.NOT_FOUND;
            } else if (message.contains("토큰") || message.contains("인증")) {
                code = "AUTH_INVALID_TOKEN";
                status = HttpStatus.UNAUTHORIZED;
            }
        }
        
        return createErrorResponseWithCode(code, message != null ? message : "잘못된 요청입니다.", status);
    }
    
    /**
     * 특정 에러 코드로 에러 응답 생성
     */
    private ResponseEntity<Map<String, Object>> createErrorResponseWithCode(String code, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", code);
        response.put("message", message);
        response.put("data", null);
        response.put("meta", null);
        
        return ResponseEntity.status(status).body(response);
    }
}

package com.example.demo.controller;

import com.example.demo.dto.auth.AuthTokensResponse;
import com.example.demo.dto.auth.SocialLoginRequest;
import com.example.demo.dto.common.ErrorResponse;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import com.example.demo.service.SocialAuthService;
import com.example.demo.domain.enums.AuthProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "소셜 로그인 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SocialAuthService socialAuthService;
    private final JwtAuthenticationHelper jwtHelper;

    @Operation(summary = "소셜 로그인", description = "Firebase Authentication SDK에서 발급받은 ID Token을 사용하여 소셜 로그인을 수행합니다. " +
            "기존 사용자는 자동으로 로그인되며, 신규 사용자는 회원가입 후 로그인됩니다. " +
            "신규 회원가입 시에는 name 필드가 필수입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthTokensResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 지원하지 않는 provider, 사용자 이름 누락 또는 길이 초과) - 에러 코드: INVALID_REQUEST, AUTH_MISSING_NAME, VALIDATION_ERROR", 
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "사용자 이름 누락", value = "{\"code\":\"AUTH_MISSING_NAME\",\"message\":\"사용자 이름이 필요합니다.\"}"),
                    @ExampleObject(name = "사용자 이름 길이 초과", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"사용자 이름은 최대 10자까지 입력 가능합니다.\"}")
                })),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Firebase ID 토큰 - 에러 코드: AUTH_INVALID_TOKEN", 
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"AUTH_INVALID_TOKEN\",\"message\":\"유효하지 않은 Firebase ID 토큰입니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"Firebase 토큰 검증 중 오류가 발생했습니다.\"}")))
    })
    @PostMapping("/social/login")
    public ResponseEntity<?> socialLogin(@Valid @RequestBody SocialLoginRequest request) {

        // 지원하는 provider 확인
        if (request.getProvider() != AuthProvider.GOOGLE && request.getProvider() != AuthProvider.APPLE) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.builder()
                            .code("INVALID_REQUEST")
                            .message("현재는 GOOGLE과 APPLE provider만 지원합니다.")
                            .build());
        }

        try {
            AuthTokensResponse tokens;
            if (request.getProvider() == AuthProvider.GOOGLE) {
                tokens = socialAuthService.loginWithGoogle(request);
            } else { // APPLE
                tokens = socialAuthService.loginWithApple(request);
            }
            return ResponseEntity.ok(tokens);
        } catch (com.google.firebase.auth.FirebaseAuthException e) {
            // Firebase 인증 오류
            String errorCodeStr = e.getErrorCode() != null ? e.getErrorCode().toString() : "";
            String errorMessage = e.getMessage();
            String message = "유효하지 않은 Firebase ID 토큰입니다.";
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            
            // 네트워크 관련 오류 체크
            if ((errorCodeStr.contains("network") || errorCodeStr.contains("timeout") || 
                errorCodeStr.contains("unavailable") || 
                (errorMessage != null && (errorMessage.contains("network") || errorMessage.contains("timeout") || 
                 errorMessage.contains("unavailable") || errorMessage.contains("연결"))))) {
                status = HttpStatus.SERVICE_UNAVAILABLE;
                message = "Firebase 서비스에 연결할 수 없습니다. 네트워크 연결을 확인해주세요.";
                return ResponseEntity.status(status)
                        .body(ErrorResponse.builder()
                                .code("FIREBASE_NETWORK_ERROR")
                                .message(message)
                                .build());
            }
            
            return ResponseEntity.status(status)
                    .body(ErrorResponse.builder()
                            .code("AUTH_INVALID_TOKEN")
                            .message(message)
                            .build());
        } catch (Exception e) {
            // 예상치 못한 오류 (네트워크 오류 포함)
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("network") || errorMessage.contains("연결") || 
                errorMessage.contains("timeout") || errorMessage.contains("unavailable"))) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(ErrorResponse.builder()
                                .code("NETWORK_ERROR")
                                .message("네트워크 연결에 문제가 발생했습니다. 인터넷 연결을 확인해주세요.")
                                .build());
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .code("INTERNAL_SERVER_ERROR")
                            .message("Firebase 토큰 검증 중 오류가 발생했습니다.")
                            .build());
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
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthTokensResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (Authorization 헤더 누락 또는 형식 오류) - 에러 코드: INVALID_REQUEST", 
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INVALID_REQUEST\",\"message\":\"Authorization 헤더에 Bearer 토큰이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 토큰, 또는 이미 사용된 토큰 - 에러 코드: INVALID_REFRESH_TOKEN, AUTH_TOKEN_EXPIRED", 
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INVALID_REFRESH_TOKEN\",\"message\":\"유효하지 않거나 만료된 Refresh Token입니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"토큰 재발급 중 오류가 발생했습니다.\"}")))
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            // Authorization 헤더에서 Bearer 토큰 추출
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.builder()
                                .code("INVALID_REQUEST")
                                .message("Authorization 헤더에 Bearer 토큰이 필요합니다.")
                                .build());
            }
            
            String refreshToken = authorizationHeader.substring(7); // "Bearer " 제거
            AuthTokensResponse tokens = socialAuthService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            // 예상치 못한 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .code("INTERNAL_SERVER_ERROR")
                            .message("토큰 재발급 중 오류가 발생했습니다.")
                            .build());
        }
    }

    @Operation(summary = "로그아웃", description = "사용자를 로그아웃합니다. Refresh Token과 Device Token이 삭제됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            // Refresh Token 추출 (선택적)
            String refreshToken = null;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                refreshToken = authorizationHeader.substring(7);
            }

            socialAuthService.logout(userId, refreshToken);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .code("INTERNAL_SERVER_ERROR")
                            .message("로그아웃 중 오류가 발생했습니다.")
                            .build());
        }
    }

    @Operation(summary = "회원탈퇴", description = "사용자 계정을 삭제합니다. 팀장인 경우 회원탈퇴할 수 없습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원탈퇴 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장은 회원탈퇴 불가)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/user")
    public ResponseEntity<?> deleteUser() {
        try {
            Long userId = jwtHelper.getCurrentUserId();
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            socialAuthService.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .code("INTERNAL_SERVER_ERROR")
                            .message("회원탈퇴 중 오류가 발생했습니다.")
                            .build());
        }
    }

    private ResponseEntity<ErrorResponse> createUnauthorizedResponse(String message) {
        ErrorResponse error = ErrorResponse.builder()
                .code("UNAUTHORIZED")
                .message(message)
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}

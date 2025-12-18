package com.example.demo.controller;

import com.example.demo.dto.common.ErrorResponse;
import com.example.demo.dto.user.UserProfileResponse;
import com.example.demo.dto.user.UserProfileUpdateRequest;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import com.example.demo.service.UserService;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 관리", description = "사용자 프로필 조회 및 수정 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtAuthenticationHelper jwtHelper;

    @Operation(summary = "내 프로필 조회", description = "현재 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UserProfileResponse.class),
                examples = @ExampleObject(value = "{\n" +
                    "  \"userId\": 1,\n" +
                    "  \"userName\": \"홍길동\",\n" +
                    "  \"userEmail\": \"user@example.com\",\n" +
                    "  \"userImageUrl\": \"https://firebasestorage.googleapis.com/v0/b/your-project.appspot.com/o/users%2F...\"\n" +
                    "}"))),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음 - 에러 코드: NOT_FOUND",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"NOT_FOUND\",\"message\":\"사용자를 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"프로필 조회 중 오류가 발생했습니다.\"}")))
    })
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            UserProfileResponse response = userService.getUserProfile(userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("프로필 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "내 프로필 수정", description = "현재 사용자의 이름을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UserProfileResponse.class),
                examples = @ExampleObject(value = "{\n" +
                    "  \"userId\": 1,\n" +
                    "  \"userName\": \"홍길동(수정)\",\n" +
                    "  \"userEmail\": \"user@example.com\",\n" +
                    "  \"userImageUrl\": \"https://firebasestorage.googleapis.com/v0/b/your-project.appspot.com/o/users%2F...\"\n" +
                    "}"))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패, 이름 필수 또는 최대 길이 초과) - 에러 코드: VALIDATION_ERROR",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "이름 필수", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"사용자 이름은 필수입니다.\"}"),
                    @ExampleObject(name = "이름 길이 초과", value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"사용자 이름은 최대 10자까지 입력 가능합니다.\"}")
                })),
        @ApiResponse(responseCode = "401", description = "인증 실패 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음 - 에러 코드: NOT_FOUND",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"NOT_FOUND\",\"message\":\"사용자를 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"프로필 수정 중 오류가 발생했습니다.\"}")))
    })
    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        try {
            Long userId = getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            UserProfileResponse response = userService.updateUserProfile(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("프로필 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Long getUserIdFromHeader(String authorizationHeader) {
        return jwtHelper.getUserIdFromHeader(authorizationHeader);
    }

    private ResponseEntity<ErrorResponse> createUnauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .code("UNAUTHORIZED")
                        .message(message)
                        .build());
    }

    private ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = e.getMessage();
        String code = "BAD_REQUEST";
        
        if (message.contains("NOT_FOUND")) {
            code = "NOT_FOUND";
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .code(code)
                        .message(message)
                        .build());
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .code(status.name())
                        .message(message)
                        .build());
    }
}


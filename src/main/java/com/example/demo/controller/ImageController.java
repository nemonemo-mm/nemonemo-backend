package com.example.demo.controller;

import com.example.demo.domain.entity.Team;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.common.ErrorResponse;
import com.example.demo.dto.image.ImageUploadResponse;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import com.example.demo.service.FirebaseStorageService;
import com.example.demo.service.TeamPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "이미지", description = "이미지 업로드 API")
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final FirebaseStorageService firebaseStorageService;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamPermissionService teamPermissionService;
    private final JwtAuthenticationHelper jwtHelper;

    @Operation(summary = "프로필 이미지 업로드", description = "사용자 프로필 이미지를 업로드합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이미지 업로드 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ImageUploadResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 없음, 잘못된 형식, 크기 초과) - 에러 코드: VALIDATION_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"이미지 파일은 jpg, jpeg, png, gif, webp 형식만 지원하며 최대 5MB까지 업로드 가능합니다.\"}"))),
        @ApiResponse(responseCode = "401", description = "인증 필요 - 에러 코드: UNAUTHORIZED",
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
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"프로필 이미지 업로드 중 오류가 발생했습니다.\"}")))
    })
    @PostMapping(value = "/users/me/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadUserProfileImage(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(
                description = "이미지 파일 (jpg, jpeg, png, gif, webp, 최대 5MB)",
                required = true,
                content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ) @RequestParam("file") MultipartFile file) {
        try {
            Long userId = jwtHelper.getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            // 기존 이미지 URL 가져오기
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND: 사용자를 찾을 수 없습니다."));
            String oldImageUrl = user.getImageUrl();

            // 새 이미지 업로드
            String newImageUrl = firebaseStorageService.updateImage(file, oldImageUrl, "users");

            // 사용자 정보 업데이트
            user.setImageUrl(newImageUrl);
            userRepository.save(user);

            return ResponseEntity.ok(ImageUploadResponse.builder()
                    .userId(userId)
                    .imageUrl(newImageUrl)
                    .build());
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("프로필 이미지 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Operation(summary = "팀 이미지 업로드", description = "팀 이미지를 업로드합니다. 팀장만 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이미지 업로드 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ImageUploadResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 없음, 잘못된 형식, 크기 초과) - 에러 코드: VALIDATION_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"VALIDATION_ERROR\",\"message\":\"이미지 파일은 jpg, jpeg, png, gif, webp 형식만 지원하며 최대 5MB까지 업로드 가능합니다.\"}"))),
        @ApiResponse(responseCode = "401", description = "인증 필요 - 에러 코드: UNAUTHORIZED",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장만 가능) - 에러 코드: FORBIDDEN",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"FORBIDDEN\",\"message\":\"팀장만 이미지를 업로드할 수 있습니다.\"}"))),
        @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음 - 에러 코드: TEAM_NOT_FOUND",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"TEAM_NOT_FOUND\",\"message\":\"팀을 찾을 수 없습니다.\"}"))),
        @ApiResponse(responseCode = "500", description = "서버 오류 - 에러 코드: INTERNAL_SERVER_ERROR",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"팀 이미지 업로드 중 오류가 발생했습니다.\"}")))
    })
    @PostMapping(value = "/teams/{teamId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadTeamImage(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long teamId,
            @Parameter(
                description = "이미지 파일 (jpg, jpeg, png, gif, webp, 최대 5MB)",
                required = true,
                content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            ) @RequestParam("file") MultipartFile file) {
        try {
            Long userId = jwtHelper.getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            // 팀장 권한 확인
            Team team = teamPermissionService.getTeamWithOwnerCheck(userId, teamId);
            String oldImageUrl = team.getImageUrl();

            // 새 이미지 업로드
            String newImageUrl = firebaseStorageService.updateImage(file, oldImageUrl, "teams");

            // 팀 정보 업데이트
            team.setImageUrl(newImageUrl);
            teamRepository.save(team);

            return ResponseEntity.ok(ImageUploadResponse.builder()
                    .teamId(teamId)
                    .imageUrl(newImageUrl)
                    .build());
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀 이미지 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * IllegalArgumentException 처리 (권한, 리소스 없음 등을 구분)
     */
    private ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = e.getMessage();
        
        // 에러 코드가 포함된 경우 (예: "FORBIDDEN: ...", "NOT_FOUND: ...")
        if (message != null && message.contains(":")) {
            String errorCode = message.split(":")[0].trim();
            String cleanMessage = message.split(":", 2)[1].trim();
            
            HttpStatus status;
            if ("FORBIDDEN".equals(errorCode)) {
                status = HttpStatus.FORBIDDEN;
            } else if ("TEAM_NOT_FOUND".equals(errorCode) || "TEAM_MEMBER_NOT_FOUND".equals(errorCode) 
                    || "POSITION_NOT_FOUND".equals(errorCode) || "NOT_FOUND".equals(errorCode)) {
                status = HttpStatus.NOT_FOUND;
            } else {
                status = HttpStatus.BAD_REQUEST;
            }
            
            return ResponseEntity.status(status)
                    .body(ErrorResponse.builder()
                            .code(errorCode)
                            .message(cleanMessage)
                            .build());
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
            } else if (message.contains("필수")) {
                code = "VALIDATION_ERROR";
            } else if (message.contains("최대") || message.contains("길이") || message.contains("크기")) {
                code = "VALIDATION_ERROR";
            }
        }
        
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .code(code)
                        .message(message != null ? message : "잘못된 요청입니다.")
                        .build());
    }
    
    /**
     * 에러 응답 생성
     */
    private ResponseEntity<ErrorResponse> createErrorResponse(String message, HttpStatus status) {
        String code = status == HttpStatus.INTERNAL_SERVER_ERROR ? "INTERNAL_SERVER_ERROR" : "INVALID_REQUEST";
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .code(code)
                        .message(message)
                        .build());
    }
    
    /**
     * 인증 실패 응답 생성
     */
    private ResponseEntity<ErrorResponse> createUnauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .code("UNAUTHORIZED")
                        .message(message)
                        .build());
    }
}

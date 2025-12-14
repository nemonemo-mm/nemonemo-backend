package com.example.demo.controller;

import com.example.demo.domain.entity.Team;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.image.ImageUploadResponse;
import com.example.demo.security.jwt.JwtAuthenticationHelper;
import com.example.demo.service.FirebaseStorageService;
import com.example.demo.service.TeamPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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

import java.util.HashMap;
import java.util.Map;

@Tag(name = "이미지", description = "이미지 업로드/삭제/수정 API")
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
            content = @Content(schema = @Schema(implementation = ImageUploadResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 없음, 잘못된 형식, 크기 초과)"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping(value = "/users/me/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadUserProfileImage(
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

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("code", "SUCCESS");
            result.put("message", null);
            result.put("data", ImageUploadResponse.builder().imageUrl(newImageUrl).build());
            result.put("meta", null);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("프로필 이미지 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "프로필 이미지 삭제", description = "사용자 프로필 이미지를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이미지 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @DeleteMapping("/users/me/profile")
    public ResponseEntity<Map<String, Object>> deleteUserProfileImage(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Long userId = jwtHelper.getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND: 사용자를 찾을 수 없습니다."));

            // 이미지 삭제
            if (user.getImageUrl() != null) {
                firebaseStorageService.deleteImage(user.getImageUrl());
                user.setImageUrl(null);
                userRepository.save(user);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("code", "SUCCESS");
            result.put("message", null);
            result.put("data", null);
            result.put("meta", null);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("프로필 이미지 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "팀 이미지 업로드", description = "팀 이미지를 업로드합니다. 팀장만 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이미지 업로드 성공",
            content = @Content(schema = @Schema(implementation = ImageUploadResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장만 가능)")
    })
    @PostMapping(value = "/teams/{teamId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadTeamImage(
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

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("code", "SUCCESS");
            result.put("message", null);
            result.put("data", ImageUploadResponse.builder().imageUrl(newImageUrl).build());
            result.put("meta", null);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀 이미지 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "팀 이미지 삭제", description = "팀 이미지를 삭제합니다. 팀장만 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이미지 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (팀장만 가능)")
    })
    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<Map<String, Object>> deleteTeamImage(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "팀 ID", required = true) @PathVariable Long teamId) {
        try {
            Long userId = jwtHelper.getUserIdFromHeader(authorizationHeader);
            if (userId == null) {
                return createUnauthorizedResponse("인증이 필요합니다.");
            }

            // 팀장 권한 확인
            Team team = teamPermissionService.getTeamWithOwnerCheck(userId, teamId);

            // 이미지 삭제
            if (team.getImageUrl() != null) {
                firebaseStorageService.deleteImage(team.getImageUrl());
                team.setImageUrl(null);
                teamRepository.save(team);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("code", "SUCCESS");
            result.put("message", null);
            result.put("data", null);
            result.put("meta", null);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return createErrorResponse("팀 이미지 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * IllegalArgumentException 처리 (권한, 리소스 없음 등을 구분)
     */
    private ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = e.getMessage();
        
        // 에러 코드가 포함된 경우 (예: "FORBIDDEN: ...", "NOT_FOUND: ...")
        if (message != null && message.contains(":")) {
            String errorCode = message.split(":")[0].trim();
            String cleanMessage = message.split(":", 2)[1].trim();
            
            if ("FORBIDDEN".equals(errorCode)) {
                return createErrorResponseWithCode("FORBIDDEN", cleanMessage, HttpStatus.FORBIDDEN);
            } else if ("TEAM_NOT_FOUND".equals(errorCode) || "TEAM_MEMBER_NOT_FOUND".equals(errorCode) 
                    || "POSITION_NOT_FOUND".equals(errorCode) || "NOT_FOUND".equals(errorCode)) {
                return createErrorResponseWithCode("NOT_FOUND", cleanMessage, HttpStatus.NOT_FOUND);
            } else {
                return createErrorResponseWithCode(errorCode, cleanMessage, HttpStatus.BAD_REQUEST);
            }
        }
        
        // 에러 코드가 없는 경우 메시지로 판단
        if (message != null) {
            if (message.contains("권한") || message.contains("FORBIDDEN") || message.contains("멤버만")) {
                String cleanMessage = message.replace("FORBIDDEN:", "").trim();
                return createErrorResponseWithCode("FORBIDDEN", cleanMessage, HttpStatus.FORBIDDEN);
            } else if (message.contains("찾을 수 없습니다") || message.contains("NOT_FOUND")) {
                return createErrorResponseWithCode("NOT_FOUND", message, HttpStatus.NOT_FOUND);
            } else if (message.contains("필수")) {
                return createErrorResponseWithCode("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
            } else if (message.contains("최대") || message.contains("길이") || message.contains("크기")) {
                return createErrorResponseWithCode("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
            }
        }
        
        // 기본값
        return createErrorResponseWithCode("INVALID_REQUEST", message != null ? message : "잘못된 요청입니다.", HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 에러 응답 생성
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", status == HttpStatus.INTERNAL_SERVER_ERROR ? "INTERNAL_SERVER_ERROR" : "INVALID_REQUEST");
        response.put("message", message);
        response.put("data", null);
        response.put("meta", null);
        
        return ResponseEntity.status(status).body(response);
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
    
    /**
     * 인증 실패 응답 생성
     */
    private ResponseEntity<Map<String, Object>> createUnauthorizedResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "UNAUTHORIZED");
        response.put("message", message);
        response.put("data", null);
        response.put("meta", null);
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}

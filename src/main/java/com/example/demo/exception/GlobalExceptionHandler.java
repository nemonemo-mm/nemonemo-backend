package com.example.demo.exception;

import com.example.demo.dto.common.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Validation 에러 처리 (@Valid 실패 시)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + (error.getDefaultMessage() != null ? error.getDefaultMessage() : "유효하지 않은 값입니다."))
                .collect(Collectors.joining("; "));
        
        if (errorMessage.isEmpty()) {
            errorMessage = "요청 데이터가 유효하지 않습니다.";
        }
        
        log.warn("Validation 에러: {}", errorMessage);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .code("VALIDATION_ERROR")
                        .message(errorMessage)
                        .build());
    }

    /**
     * JSON 파싱 오류 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("JSON 파싱 오류: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .code("INVALID_JSON")
                        .message("요청 본문의 JSON 형식이 올바르지 않습니다.")
                        .build());
    }

    /**
     * IllegalArgumentException 처리
     * Controller에서 처리하지 못한 IllegalArgumentException을 전역으로 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException 발생: {}", ex.getMessage(), ex);
        
        String message = ex.getMessage();
        String code;
        HttpStatus status;
        
        // 에러 코드가 포함된 경우 (예: "FORBIDDEN: ...", "NOT_FOUND: ...")
        if (message != null && message.contains(":")) {
            String errorCode = message.split(":")[0].trim();
            String cleanMessage = message.split(":", 2)[1].trim();
            
            if ("FORBIDDEN".equals(errorCode)) {
                code = "FORBIDDEN";
                status = HttpStatus.FORBIDDEN;
            } else if ("TEAM_NOT_FOUND".equals(errorCode) || "TEAM_MEMBER_NOT_FOUND".equals(errorCode) 
                    || "POSITION_NOT_FOUND".equals(errorCode) || "NOT_FOUND".equals(errorCode)) {
                code = "NOT_FOUND";
                status = HttpStatus.NOT_FOUND;
            } else if ("AUTH_INVALID_TOKEN".equals(errorCode) || "INVALID_REFRESH_TOKEN".equals(errorCode) 
                    || "AUTH_TOKEN_EXPIRED".equals(errorCode)) {
                code = errorCode;
                status = HttpStatus.UNAUTHORIZED;
            } else if ("DEFAULT_POSITION_CANNOT_DELETE".equals(errorCode)) {
                code = "INVALID_REQUEST";
                status = HttpStatus.BAD_REQUEST;
            } else {
                code = errorCode;
                status = HttpStatus.BAD_REQUEST;
            }
            
            return ResponseEntity.status(status)
                    .body(ErrorResponse.builder()
                            .code(code)
                            .message(cleanMessage)
                            .build());
        } else {
            // 에러 코드가 없는 경우 메시지로 판단
            code = "INVALID_REQUEST";
            status = HttpStatus.BAD_REQUEST;
            
            if (message != null) {
                if (message.contains("권한") || message.contains("FORBIDDEN")) {
                    code = "FORBIDDEN";
                    status = HttpStatus.FORBIDDEN;
                } else if (message.contains("찾을 수 없습니다") || message.contains("NOT_FOUND")) {
                    code = "NOT_FOUND";
                    status = HttpStatus.NOT_FOUND;
                } else if (message.contains("최대") && message.contains("자")) {
                    code = "VALIDATION_ERROR";
                } else if (message.contains("필수")) {
                    code = "INVALID_REQUEST";
                }
            }
            
            return ResponseEntity.status(status)
                    .body(ErrorResponse.builder()
                            .code(code)
                            .message(message != null ? message : "잘못된 요청입니다.")
                            .build());
        }
    }

    /**
     * 데이터베이스 제약조건 위반 예외 처리 (UNIQUE, FOREIGN KEY 등)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("데이터베이스 제약조건 위반: {}", ex.getMessage(), ex);
        
        String message = ex.getMessage();
        String errorMessage = "데이터 저장 중 오류가 발생했습니다.";
        
        // 제약조건 위반 메시지 파싱
        if (message != null) {
            if (message.contains("unique constraint") || message.contains("UNIQUE")) {
                if (message.contains("uq_team_member_team_user")) {
                    errorMessage = "이미 해당 팀의 멤버입니다.";
                } else if (message.contains("uq_device_token_user_id")) {
                    errorMessage = "이미 디바이스 토큰이 등록되어 있습니다.";
                } else if (message.contains("uq_device_token_token")) {
                    errorMessage = "이미 사용 중인 디바이스 토큰입니다.";
                } else if (message.contains("uq_position_team_name")) {
                    errorMessage = "해당 팀에 이미 같은 이름의 포지션이 존재합니다.";
                } else if (message.contains("email") || message.contains("app_user")) {
                    errorMessage = "이미 사용 중인 이메일입니다.";
                } else {
                    errorMessage = "중복된 데이터입니다.";
                }
            } else if (message.contains("foreign key") || message.contains("FOREIGN KEY")) {
                errorMessage = "관련된 데이터가 존재하여 삭제할 수 없습니다.";
            } else if (message.contains("check constraint") || message.contains("CHECK")) {
                errorMessage = "데이터 제약조건을 위반했습니다.";
            }
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .code("DATA_INTEGRITY_VIOLATION")
                        .message(errorMessage)
                        .build());
    }

    /**
     * 네트워크 연결 실패 예외 처리 (UnknownHostException)
     */
    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity<ErrorResponse> handleUnknownHostException(UnknownHostException ex) {
        log.error("네트워크 연결 실패: 서버를 찾을 수 없습니다. {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.builder()
                        .code("NETWORK_ERROR")
                        .message("네트워크 연결에 실패했습니다. 인터넷 연결을 확인해주세요.")
                        .build());
    }

    /**
     * 네트워크 타임아웃 예외 처리 (SocketTimeoutException)
     */
    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleSocketTimeoutException(SocketTimeoutException ex) {
        log.error("네트워크 타임아웃: 서버 응답 시간이 초과되었습니다. {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.builder()
                        .code("NETWORK_TIMEOUT")
                        .message("서버 응답 시간이 초과되었습니다. 네트워크 연결을 확인하고 잠시 후 다시 시도해주세요.")
                        .build());
    }

    /**
     * 연결 거부 예외 처리 (ConnectException)
     */
    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<ErrorResponse> handleConnectException(ConnectException ex) {
        log.error("연결 거부: 서버에 연결할 수 없습니다. {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.builder()
                        .code("CONNECTION_ERROR")
                        .message("서버에 연결할 수 없습니다. 네트워크 연결을 확인해주세요.")
                        .build());
    }

    /**
     * IOException 처리 (네트워크 I/O 오류 등)
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        log.error("I/O 오류 발생: {}", ex.getMessage(), ex);
        
        Throwable cause = ex.getCause();
        if (cause instanceof UnknownHostException) {
            return handleUnknownHostException((UnknownHostException) cause);
        } else if (cause instanceof SocketTimeoutException) {
            return handleSocketTimeoutException((SocketTimeoutException) cause);
        } else if (cause instanceof ConnectException) {
            return handleConnectException((ConnectException) cause);
        }
        
        String message = ex.getMessage();
        if (message != null && (message.contains("네트워크") || message.contains("연결") || message.contains("timeout"))) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ErrorResponse.builder()
                            .code("NETWORK_ERROR")
                            .message("네트워크 연결에 문제가 발생했습니다. 인터넷 연결을 확인하고 잠시 후 다시 시도해주세요.")
                            .build());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .code("IO_ERROR")
                        .message("입출력 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                        .build());
    }

    /**
     * IllegalStateException 처리 (Firebase Storage 등 외부 서비스 오류)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        String message = ex.getMessage();
        
        // 에러 코드가 포함된 경우 (예: "STORAGE_NETWORK_ERROR: ...")
        if (message != null && message.contains(":")) {
            String errorCode = message.split(":")[0].trim();
            String cleanMessage = message.split(":", 2)[1].trim();
            
            HttpStatus status;
            if ("STORAGE_PERMISSION_DENIED".equals(errorCode)) {
                status = HttpStatus.FORBIDDEN;
            } else if ("STORAGE_NOT_FOUND".equals(errorCode)) {
                status = HttpStatus.NOT_FOUND;
            } else if ("STORAGE_NETWORK_ERROR".equals(errorCode) || 
                       "STORAGE_TIMEOUT".equals(errorCode) || 
                       "STORAGE_CONNECTION_ERROR".equals(errorCode)) {
                status = HttpStatus.SERVICE_UNAVAILABLE;
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            
            log.warn("외부 서비스 오류: {} - {}", errorCode, cleanMessage);
            
            return ResponseEntity.status(status)
                    .body(ErrorResponse.builder()
                            .code(errorCode)
                            .message(cleanMessage)
                            .build());
        }
        
        log.warn("IllegalStateException 발생: {}", message);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .code("SERVICE_ERROR")
                        .message(message != null ? message : "서비스 오류가 발생했습니다.")
                        .build());
    }

    /**
     * 모든 예외를 처리하는 최종 핸들러 (예상치 못한 예외)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("예상치 못한 예외 발생: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .code("INTERNAL_SERVER_ERROR")
                        .message("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                        .build());
    }
}


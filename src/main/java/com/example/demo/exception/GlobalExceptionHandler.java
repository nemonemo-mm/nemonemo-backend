package com.example.demo.exception;

import com.example.demo.dto.common.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

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
                } else if (message.contains("최대") && (message.contains("10자") || message.contains("길이"))) {
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
}


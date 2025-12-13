package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Validation 에러 처리 (@Valid 실패 시)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "유효하지 않은 값입니다.",
                    (existing, replacement) -> existing + ", " + replacement
                ));
        
        String errorMessage = fieldErrors.isEmpty() 
            ? "요청 데이터가 유효하지 않습니다."
            : fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));
        
        response.put("success", false);
        response.put("code", "VALIDATION_ERROR");
        response.put("message", errorMessage);
        response.put("data", fieldErrors);
        response.put("meta", null);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * JSON 파싱 오류 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "INVALID_JSON");
        response.put("message", "요청 본문의 JSON 형식이 올바르지 않습니다: " + ex.getMessage());
        response.put("data", null);
        response.put("meta", null);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        
        String message = ex.getMessage();
        String code = "INVALID_REQUEST";
        
        // 메시지에 따라 적절한 코드 설정
        if (message != null) {
            if (message.contains("최대") && (message.contains("10자") || message.contains("길이"))) {
                code = "VALIDATION_ERROR";
            } else if (message.contains("찾을 수 없습니다")) {
                code = "NOT_FOUND";
            } else if (message.contains("권한")) {
                code = "FORBIDDEN";
            } else if (message.contains("필수")) {
                code = "INVALID_REQUEST";
            }
        }
        
        response.put("code", code);
        response.put("message", message);
        response.put("data", null);
        response.put("meta", null);
        
        HttpStatus status = "FORBIDDEN".equals(code) ? HttpStatus.FORBIDDEN 
                : "NOT_FOUND".equals(code) ? HttpStatus.NOT_FOUND 
                : HttpStatus.BAD_REQUEST;
        
        return ResponseEntity.status(status).body(response);
    }
}


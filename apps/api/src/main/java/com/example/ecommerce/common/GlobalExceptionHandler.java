package com.example.ecommerce.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalState(IllegalStateException ex) {
        String message = ex.getMessage() == null ? "请求处理失败" : ex.getMessage();
        if (message.contains("未登录") || message.contains("需要用户登录")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(message));
        }
        if (message.contains("需要管理员权限")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(message));
        }
        return ResponseEntity.badRequest().body(ApiResponse.fail(message));
    }
}

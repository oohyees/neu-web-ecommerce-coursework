package com.example.ecommerce.product;

public record ApiResponse<T>(boolean success, String message, T data) {
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, "ok", data); }
}

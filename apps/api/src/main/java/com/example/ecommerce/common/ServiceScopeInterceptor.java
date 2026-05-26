package com.example.ecommerce.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ServiceScopeInterceptor implements HandlerInterceptor {
    @Value("${app.service-scope:all}")
    private String scope;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("all".equals(scope) || "OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String path=request.getRequestURI();
        boolean allowed=switch(scope){
            case "auth" -> path.startsWith("/api/auth");
            case "product" -> path.startsWith("/api/products") || path.startsWith("/api/categories") || path.startsWith("/api/marketing") || path.startsWith("/api/home") || path.startsWith("/api/announcements") || path.startsWith("/api/activity-notices") || path.startsWith("/api/reviews") || path.startsWith("/api/admin/activity-notices");
            case "order" -> path.startsWith("/api/orders") || path.startsWith("/api/admin/orders") || path.startsWith("/api/cart") || path.startsWith("/api/addresses") || path.startsWith("/api/consultations") || path.startsWith("/api/admin/consultations");
            default -> true;
        };
        if(!allowed){
            response.setStatus(404);
            return false;
        }
        return true;
    }
}

package com.example.ecommerce.common;

import com.example.ecommerce.model.SessionInfo;
import com.example.ecommerce.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final SessionService sessionService;
    public AuthInterceptor(SessionService sessionService) { this.sessionService = sessionService; }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        String method = request.getMethod();
        boolean authPublic = path.equals("/api/auth/login") || path.equals("/api/auth/register")
                || path.equals("/api/auth/register/email") || path.equals("/api/auth/code")
                || path.equals("/api/auth/password/reset") || path.equals("/api/auth/admin/login")
                || path.equals("/api/auth/logout");
        boolean publicPath = authPublic
                || ("GET".equals(method) && (
                    path.equals("/api/products")
                    || path.matches("/api/products/\\d+")
                    || path.equals("/api/categories")
                    || path.equals("/api/home")
                    || path.equals("/api/home/banners")
                    || path.equals("/api/announcements")
                    || path.equals("/api/activity-notices")
                    || (path.startsWith("/api/marketing") && !path.startsWith("/api/marketing/admin"))
                    || path.equals("/api/reviews")
                ));
        if (publicPath) return true;
        String token = request.getHeader("Authorization");
        SessionInfo session = token == null ? null : sessionService.get(token.replace("Bearer ", ""));
        if (session == null) {
            response.setStatus(401);
            return false;
        }
        if (path.startsWith("/api/admin") && !java.util.Set.of("ADMIN","SUPER_ADMIN").contains(session.role())) {
            response.setStatus(403);
            return false;
        }
        boolean superAdminOnly = path.startsWith("/api/admin/users")
                || path.startsWith("/api/admin/activity-notices")
                || path.startsWith("/api/admin/announcements");
        if (superAdminOnly && !"SUPER_ADMIN".equals(session.role())) {
            response.setStatus(403);
            return false;
        }
        request.setAttribute("session", session);
        return true;
    }
}

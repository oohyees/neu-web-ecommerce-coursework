package com.example.ecommerce.common;

import com.example.ecommerce.model.SessionInfo;
import com.example.ecommerce.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final SessionService sessionService;

    public AuthInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()))
            return true;

        String path = request.getRequestURI();
        String method = request.getMethod();

        boolean authPublic = path.equals("/api/auth/login") || path.equals("/api/auth/register")
                || path.equals("/api/auth/register/email") || path.equals("/api/auth/code")
                || path.equals("/api/auth/password/reset") || path.equals("/api/auth/admin/login")
                || path.equals("/api/auth/logout");
        boolean publicPath = authPublic
                || ("GET".equals(method) && (path.equals("/api/products")
                        || path.matches("/api/products/\\d+")
                        || path.equals("/api/categories")
                        || path.equals("/api/home")
                        || path.equals("/api/home/banners")
                        || path.equals("/api/announcements")
                        || path.equals("/api/activity-notices")
                        || path.matches("/api/marketing/specs/\\d+")
                        || path.equals("/api/marketing/coupons")
                        || path.equals("/api/marketing/promotions")
                        || path.equals("/api/reviews")));
        if (publicPath)
            return true;

        String token = request.getHeader("Authorization");
        SessionInfo session = token == null ? null
                : sessionService.get(token.replace("Bearer ", ""));
        if (session == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"未登录或会话已过期，请重新登录\"}");
            return false;
        }

        if (isAdminPath(path, method) && !Set.of("ADMIN", "SUPER_ADMIN").contains(session.role())) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"权限不足，仅管理员可操作\"}");
            return false;
        }

        boolean superAdminOnly = path.startsWith("/api/auth/admin/users")
                || path.startsWith("/api/auth/admin/admins")
                || path.startsWith("/api/admin/users")
                || path.startsWith("/api/admin/activity-notices")
                || path.startsWith("/api/admin/announcements");
        if (superAdminOnly && !"SUPER_ADMIN".equals(session.role())) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"权限不足，仅超级管理员可操作\"}");
            return false;
        }

        request.setAttribute("session", session);

        // Bind Shiro Subject so @RequiresRoles / @RequiresPermissions work
        bindShiroSubject(session);

        return true;
    }

    /**
     * Create an authenticated Shiro Subject from the Redis session so that
     * Shiro annotations on controllers are enforced.
     */
    private void bindShiroSubject(SessionInfo session) {
        try {
            Subject currentSubject = SecurityUtils.getSubject();
            if (currentSubject.getPrincipal() == null) {
                currentSubject.login(new SessionToken(session));
            }
        } catch (Exception ignored) {
            // Subject already exists or login failed — continue without Shiro binding
        }
    }

    private boolean isAdminPath(String path, String method) {
        return path.startsWith("/api/admin")
                || path.startsWith("/api/auth/admin")
                || path.startsWith("/api/products/admin")
                || path.startsWith("/api/marketing/admin")
                || path.startsWith("/api/reviews/admin")
                || (path.startsWith("/api/home/banners") && !"GET".equalsIgnoreCase(method));
    }
}

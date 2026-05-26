package com.example.ecommerce.common;

import com.example.ecommerce.model.SessionInfo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

public final class CurrentSession {
    private CurrentSession() {
    }

    public static SessionInfo get(HttpServletRequest request) {
        Object session = request.getAttribute("session");
        if (session instanceof SessionInfo info) return info;
        throw new IllegalStateException("未登录或会话已过期");
    }

    public static long userId(HttpServletRequest request) {
        SessionInfo session = get(request);
        if (!"USER".equals(session.role())) throw new IllegalStateException("需要用户登录");
        return session.id();
    }

    public static long adminId(HttpServletRequest request) {
        SessionInfo session = get(request);
        if (!Set.of("ADMIN", "SUPER_ADMIN").contains(session.role())) throw new IllegalStateException("需要管理员权限");
        return session.id();
    }
}

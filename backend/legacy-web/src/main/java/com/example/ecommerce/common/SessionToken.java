package com.example.ecommerce.common;

import com.example.ecommerce.model.SessionInfo;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * Shiro AuthenticationToken backed by a Redis SessionInfo.
 * Used by AuthInterceptor to create a Shiro Subject after Bearer token validation,
 * enabling {@code @RequiresRoles} / {@code @RequiresPermissions} annotations.
 */
public class SessionToken implements AuthenticationToken {
    private final SessionInfo session;

    public SessionToken(SessionInfo session) {
        this.session = session;
    }

    @Override
    public Object getPrincipal() {
        return session.id().toString();
    }

    @Override
    public Object getCredentials() {
        return session;
    }

    public SessionInfo getSession() {
        return session;
    }
}

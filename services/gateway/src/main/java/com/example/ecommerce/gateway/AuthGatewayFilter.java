package com.example.ecommerce.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class AuthGatewayFilter implements GlobalFilter, Ordered {
    private static final Set<String> ADMIN_ROLES = Set.of("ADMIN", "SUPER_ADMIN");
    private final StringRedisTemplate redis;

    public AuthGatewayFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        if ("OPTIONS".equalsIgnoreCase(method) || isPublic(path, method)) {
            return chain.filter(exchange);
        }

        String authorization = request.getHeaders().getFirst("Authorization");
        String token = authorization == null ? "" : authorization.replaceFirst("Bearer\\s+", "");
        String session = token.isBlank() ? null : redis.opsForValue().get("session:" + token);
        if (session == null || session.isBlank()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String[] parts = session.split(":", 2);
        String userId = parts[0];
        String role = parts.length > 1 ? parts[1] : "USER";
        if (isAdminPath(path) && !ADMIN_ROLES.contains(role)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        ServerHttpRequest mutated = request.mutate()
                .header("X-User-Id", userId)
                .header("X-Role", role)
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublic(String path, String method) {
        if (path.equals("/api/auth/login")
                || path.equals("/api/auth/admin/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/register/email")
                || path.equals("/api/auth/code")
                || path.equals("/api/auth/password/reset")) {
            return true;
        }
        if (!"GET".equalsIgnoreCase(method)) return false;
        return path.equals("/api/products")
                || path.matches("/api/products/\\d+")
                || path.equals("/api/categories")
                || path.equals("/api/home")
                || path.equals("/api/home/banners")
                || path.equals("/api/announcements")
                || path.equals("/api/activity-notices")
                || path.matches("/api/marketing/specs/\\d+")
                || path.equals("/api/marketing/promotions")
                || path.equals("/api/marketing/coupons")
                || path.equals("/api/reviews");
    }

    private boolean isAdminPath(String path) {
        return path.startsWith("/api/admin")
                || path.startsWith("/api/auth/admin")
                || path.startsWith("/api/products/admin")
                || path.startsWith("/api/marketing/admin")
                || path.startsWith("/api/reviews/admin");
    }
}

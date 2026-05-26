package com.example.ecommerce.legacy;

import com.example.ecommerce.model.SessionInfo;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class LegacyAuditFilter implements Filter {
    public static final String AUDIT_LOGS = "legacyAuditLogs";
    private static final int MAX_LOGS = 50;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        long started = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            if (request instanceof HttpServletRequest http) {
                record(http, System.currentTimeMillis() - started);
            }
        }
    }

    private void record(HttpServletRequest request, long costMs) {
        ServletContext context = request.getServletContext();
        synchronized (context) {
            @SuppressWarnings("unchecked")
            Deque<String> logs = (Deque<String>) context.getAttribute(AUDIT_LOGS);
            if (logs == null) logs = new ArrayDeque<>();
            Object session = request.getAttribute("session");
            String principal = session instanceof SessionInfo info ? info.role() + "#" + info.id() : "ANONYMOUS";
            logs.addFirst(LocalDateTime.now() + " " + request.getMethod() + " " + request.getRequestURI() + " " + principal + " " + costMs + "ms");
            while (logs.size() > MAX_LOGS) logs.removeLast();
            context.setAttribute(AUDIT_LOGS, logs);
        }
    }

    public static List<String> logs(ServletContext context) {
        Object value = context.getAttribute(AUDIT_LOGS);
        if (value instanceof Deque<?> deque) {
            List<String> result = new ArrayList<>();
            for (Object item : deque) result.add(String.valueOf(item));
            return result;
        }
        return List.of();
    }
}

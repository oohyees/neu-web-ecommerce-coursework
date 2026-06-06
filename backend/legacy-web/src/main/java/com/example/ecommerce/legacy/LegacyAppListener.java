package com.example.ecommerce.legacy;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class LegacyAppListener implements ServletContextListener, HttpSessionListener {
    public static final String START_TIME = "legacyStartTime";
    public static final String ONLINE_USERS = "legacyOnlineUsers";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        context.setAttribute(START_TIME, LocalDateTime.now().toString());
        context.setAttribute(ONLINE_USERS, new AtomicInteger(0));
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        counter(se).incrementAndGet();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        counter(se).updateAndGet(value -> Math.max(0, value - 1));
    }

    private AtomicInteger counter(HttpSessionEvent se) {
        Object value = se.getSession().getServletContext().getAttribute(ONLINE_USERS);
        if (value instanceof AtomicInteger counter) return counter;
        AtomicInteger counter = new AtomicInteger(0);
        se.getSession().getServletContext().setAttribute(ONLINE_USERS, counter);
        return counter;
    }
}

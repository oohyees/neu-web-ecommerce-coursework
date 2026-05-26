package com.example.ecommerce.legacy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
public class LegacyWebConfig {
    @Bean
    public ServletRegistrationBean<LegacyStatusServlet> legacyStatusServlet(LegacyJdbcDao jdbcDao, ObjectMapper objectMapper) {
        return new ServletRegistrationBean<>(new LegacyStatusServlet(jdbcDao, objectMapper), "/legacy/servlet/status", "/legacy/status");
    }

    @Bean
    public FilterRegistrationBean<LegacyAuditFilter> legacyAuditFilter() {
        FilterRegistrationBean<LegacyAuditFilter> bean = new FilterRegistrationBean<>(new LegacyAuditFilter());
        bean.addUrlPatterns("/api/*", "/legacy/*");
        bean.setOrder(100);
        return bean;
    }

    @Bean
    public ServletListenerRegistrationBean<LegacyAppListener> legacyAppListener() {
        return new ServletListenerRegistrationBean<>(new LegacyAppListener());
    }

    @Bean
    public ViewResolver legacyJspViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");
        resolver.setOrder(10);
        return resolver;
    }
}

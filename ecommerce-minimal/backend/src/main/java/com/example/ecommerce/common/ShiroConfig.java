package com.example.ecommerce.common;

import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@Profile("shiro")
public class ShiroConfig {

    @Bean
    public ShiroRealm shiroRealm(com.example.ecommerce.mapper.AuthMapper authMapper) {
        return new ShiroRealm(authMapper);
    }

    @Bean
    public SecurityManager securityManager(ShiroRealm realm) {
        DefaultWebSecurityManager sm = new DefaultWebSecurityManager();
        sm.setRealm(realm);
        return sm;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean filter = new ShiroFilterFactoryBean();
        filter.setSecurityManager(securityManager);
        filter.setLoginUrl("/api/auth/login");

        Map<String, String> chains = new LinkedHashMap<>();
        chains.put("/api/auth/login", "anon");
        chains.put("/api/auth/register", "anon");
        chains.put("/api/auth/register/email", "anon");
        chains.put("/api/auth/code", "anon");
        chains.put("/api/auth/password/reset", "anon");
        chains.put("/api/auth/admin/login", "anon");
        chains.put("/api/auth/logout", "anon");
        chains.put("/api/products/**", "anon");
        chains.put("/api/categories/**", "anon");
        chains.put("/api/home/**", "anon");
        chains.put("/api/announcements/**", "anon");
        chains.put("/api/activity-notices/**", "anon");
        chains.put("/api/marketing/**", "anon");
        chains.put("/api/reviews/**", "anon");
        chains.put("/api/files/upload", "anon");
        chains.put("/api/admin/**", "authc");
        chains.put("/api/auth/admin/**", "authc");
        chains.put("/api/cart/**", "authc");
        chains.put("/api/orders/**", "authc");
        chains.put("/api/addresses/**", "authc");
        chains.put("/api/favorites/**", "authc");
        chains.put("/api/feedback/**", "authc");
        chains.put("/api/consultations/**", "authc");
        chains.put("/api/auth/profile", "authc");
        chains.put("/api/auth/password", "authc");
        chains.put("/**", "authc");

        filter.setFilterChainDefinitionMap(chains);
        return filter;
    }
}

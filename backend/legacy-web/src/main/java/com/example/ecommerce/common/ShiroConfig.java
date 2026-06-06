package com.example.ecommerce.common;

import com.example.ecommerce.mapper.AuthMapper;
import com.example.ecommerce.service.AuthService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shiro configuration — always active.
 * Request-level authentication (Bearer tokens) is handled by {@link AuthInterceptor}.
 * Shiro provides:
 * - Authentication realm ({@link ShiroRealm}) for UsernamePasswordToken and SessionToken
 * - Authorization via {@code @RequiresRoles} / {@code @RequiresPermissions} annotations
 * No Shiro web filter is registered — AuthInterceptor validates every request.
 */
@Configuration
public class ShiroConfig {

    @Bean
    public ShiroRealm shiroRealm(AuthService authService, AuthMapper authMapper) {
        return new ShiroRealm(authService, authMapper);
    }

    @Bean
    public SecurityManager securityManager(Realm realm) {
        DefaultSecurityManager sm = new DefaultSecurityManager();
        sm.setRealm(realm);
        SecurityUtils.setSecurityManager(sm);
        return sm;
    }

    /**
     * Enables Shiro's {@code @RequiresRoles}, {@code @RequiresPermissions} etc. on Spring beans.
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    /**
     * Required for Shiro annotation AOP to work with Spring's proxy-based beans.
     */
    @Bean
    public static DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    /**
     * Shiro lifecycle bean processor — handles init/destroy for Shiro components.
     */
    @Bean
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }
}

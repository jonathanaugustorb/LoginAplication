package com.example.LoginCompleto.infra.security;

import com.example.LoginCompleto.user.entity.enums.UserRole;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private static final String LOGIN_PAGE = "/login";
    private static final int UNLIMITED_SESSIONS = -1;

    private static final String[] PUBLIC_PATHS = {
            LOGIN_PAGE,
            "/register",
            "/forgot-password",
            "/reset-password",
            "/css/**",
            "/js/**",
            "/images/**",
            "/webjars/**",
            "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage(LOGIN_PAGE)
                        .defaultSuccessUrl("/", true)
                        .failureUrl(LOGIN_PAGE + "?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl(LOGIN_PAGE + "?logout")
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(session -> session
                        .maximumSessions(UNLIMITED_SESSIONS)
                        .sessionRegistry(sessionRegistry)
                        .expiredUrl(LOGIN_PAGE + "?expired"))
                .headers(headers -> headers
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.SAME_ORIGIN)))
                .build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public static RoleHierarchy roleHierarchy() {
        var builder = RoleHierarchyImpl.withDefaultRolePrefix();
        UserRole[] roles = UserRole.values();

        for (int index = roles.length - 1; index > 0; index--) {
            builder.role(roles[index].name()).implies(roles[index - 1].name());
        }

        return builder.build();
    }
}

package com.example.LoginCompleto.infra.security;

import com.example.LoginCompleto.user.entity.User;
import com.example.LoginCompleto.user.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.session.SessionRegistryImpl;

import static org.assertj.core.api.Assertions.assertThat;

class UserSessionServiceTest {

    private SessionRegistryImpl sessionRegistry;
    private UserSessionService userSessionService;

    @BeforeEach
    void setUp() {
        sessionRegistry = new SessionRegistryImpl();
        userSessionService = new UserSessionService(sessionRegistry);

        sessionRegistry.registerNewSession("joao-1", user("joao"));
        sessionRegistry.registerNewSession("joao-2", user("joao"));
        sessionRegistry.registerNewSession("maria-1", user("maria"));
    }

    @Test
    void expiresAllSessionsOfUser() {
        userSessionService.expireSessions("joao");

        assertThat(sessionRegistry.getSessionInformation("joao-1").isExpired()).isTrue();
        assertThat(sessionRegistry.getSessionInformation("joao-2").isExpired()).isTrue();
        assertThat(sessionRegistry.getSessionInformation("maria-1").isExpired()).isFalse();
    }

    @Test
    void keepsCurrentSession() {
        userSessionService.expireSessionsExcept("joao", "joao-2");

        assertThat(sessionRegistry.getSessionInformation("joao-1").isExpired()).isTrue();
        assertThat(sessionRegistry.getSessionInformation("joao-2").isExpired()).isFalse();
    }

    private User user(String username) {
        return User.builder().username(username).role(UserRole.ESTOQUE).build();
    }
}

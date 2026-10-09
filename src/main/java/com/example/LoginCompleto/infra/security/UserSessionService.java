package com.example.LoginCompleto.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final SessionRegistry sessionRegistry;

    public void expireSessions(String username) {
        expireSessionsExcept(username, null);
    }

    public void expireSessionsExcept(String username, String keptSessionId) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> principal instanceof UserDetails details && details.getUsername().equals(username))
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .filter(session -> !Objects.equals(session.getSessionId(), keptSessionId))
                .forEach(SessionInformation::expireNow);
    }
}

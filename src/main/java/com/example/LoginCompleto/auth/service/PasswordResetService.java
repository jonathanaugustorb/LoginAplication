package com.example.LoginCompleto.auth.service;

import com.example.LoginCompleto.auth.entity.PasswordResetToken;
import com.example.LoginCompleto.auth.exception.InvalidPasswordResetTokenException;
import com.example.LoginCompleto.auth.repository.IPasswordResetTokenRepository;
import com.example.LoginCompleto.infra.config.AppProperties;
import com.example.LoginCompleto.infra.mail.IMailService;
import com.example.LoginCompleto.infra.security.SecureTokenGenerator;
import com.example.LoginCompleto.infra.security.UserSessionService;
import com.example.LoginCompleto.user.entity.User;
import com.example.LoginCompleto.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String RESET_PATH = "/reset-password";
    private static final String MAIL_SUBJECT = "Redefinição de senha";
    private static final String MAIL_TEMPLATE = "mail/password-reset";

    private final IPasswordResetTokenRepository tokenRepository;
    private final UserService userService;
    private final IMailService mailService;
    private final SecureTokenGenerator tokenGenerator;
    private final UserSessionService userSessionService;
    private final AppProperties appProperties;

    public void requestReset(String email) {
        userService.findByEmail(email)
                .filter(User::isEnabled)
                .filter(user -> !wasRecentlyRequested(user))
                .ifPresent(this::sendResetLink);
    }

    public void validateToken(String token) {
        findValidToken(token);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = findValidToken(token);

        if (tokenRepository.deleteByTokenHash(resetToken.getTokenHash()) == 0) {
            throw new InvalidPasswordResetTokenException();
        }

        User user = userService.findById(resetToken.getUserId());
        userService.updatePassword(user, newPassword);
        tokenRepository.deleteByUserId(user.getId());
        userSessionService.expireSessions(user.getUsername());
    }

    private boolean wasRecentlyRequested(User user) {
        Instant limit = Instant.now().minus(appProperties.passwordReset().resendInterval());
        return tokenRepository.existsByUserIdAndCreatedAtAfter(user.getId(), limit);
    }

    private void sendResetLink(User user) {
        tokenRepository.deleteByUserId(user.getId());

        String token = tokenGenerator.generate();
        var expiration = appProperties.passwordReset().expiration();

        tokenRepository.save(PasswordResetToken.builder()
                .tokenHash(tokenGenerator.hash(token))
                .userId(user.getId())
                .expiresAt(Instant.now().plus(expiration))
                .build());

        mailService.send(user.getEmail(), MAIL_SUBJECT, MAIL_TEMPLATE, Map.of(
                "name", user.getName(),
                "resetLink", buildResetLink(token),
                "expirationMinutes", expiration.toMinutes()));
    }

    private String buildResetLink(String token) {
        return UriComponentsBuilder.fromUriString(appProperties.baseUrl())
                .path(RESET_PATH)
                .queryParam("token", token)
                .toUriString();
    }

    private PasswordResetToken findValidToken(String token) {
        if (token == null || token.isBlank()) throw new InvalidPasswordResetTokenException();

        return tokenRepository.findByTokenHash(tokenGenerator.hash(token))
                .filter(resetToken -> !resetToken.isExpired())
                .orElseThrow(InvalidPasswordResetTokenException::new);
    }
}

package com.example.LoginCompleto.auth;

import com.example.LoginCompleto.auth.entity.PasswordResetToken;
import com.example.LoginCompleto.auth.repository.IPasswordResetTokenRepository;
import com.example.LoginCompleto.common.web.FlashMessage;
import com.example.LoginCompleto.infra.security.SecureTokenGenerator;
import com.example.LoginCompleto.support.AbstractIntegrationTest;
import com.example.LoginCompleto.support.RecordingMailService.SentMail;
import com.example.LoginCompleto.user.entity.User;
import com.example.LoginCompleto.user.entity.enums.UserRole;
import com.example.LoginCompleto.user.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class PasswordResetIntegrationTest extends AbstractIntegrationTest {

    private static final String NEW_PASSWORD = "novaSenha1";
    private static final String GENERIC_MESSAGE = "Se o e-mail estiver cadastrado";

    @Autowired
    private IPasswordResetTokenRepository tokenRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private SecureTokenGenerator tokenGenerator;

    private User joao;

    @BeforeEach
    void setUp() {
        joao = createUser("joao", UserRole.ESTOQUE);
    }

    @Test
    void sendsResetLinkAndStoresOnlyTheTokenHash() throws Exception {
        requestReset(" JOAO@mail.com ");

        assertThat(mailService.sentMails()).hasSize(1);
        SentMail mail = mailService.sentMails().get(0);
        assertThat(mail.to()).isEqualTo("joao@mail.com");
        assertThat(mail.template()).isEqualTo("mail/password-reset");
        assertThat(mail.variables().get("expirationMinutes")).isEqualTo(30L);

        String token = tokenFrom(mail);
        PasswordResetToken stored = tokenRepository.findAll().get(0);
        assertThat(stored.getTokenHash()).isEqualTo(tokenGenerator.hash(token)).isNotEqualTo(token);
        assertThat(stored.getUserId()).isEqualTo(joao.getId());
    }

    @Test
    void answersTheSameForUnknownEmailWithoutSendingMail() throws Exception {
        requestReset("ninguem@mail.com");

        assertThat(mailService.sentMails()).isEmpty();
        assertThat(tokenRepository.count()).isZero();
    }

    @Test
    void ignoresRepeatedRequestsWithinResendInterval() throws Exception {
        requestReset("joao@mail.com");
        requestReset("joao@mail.com");

        assertThat(mailService.sentMails()).hasSize(1);
        assertThat(tokenRepository.count()).isEqualTo(1);
    }

    @Test
    void doesNotSendMailToDisabledUser() throws Exception {
        joao.setEnabled(false);
        userRepository.save(joao);

        requestReset("joao@mail.com");

        assertThat(mailService.sentMails()).isEmpty();
    }

    @Test
    void resetsPasswordOnceWithValidToken() throws Exception {
        requestReset("joao@mail.com");
        String token = tokenFrom(mailService.sentMails().get(0));

        mockMvc.perform(get("/reset-password").param("token", token))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-password"));

        mockMvc.perform(resetPassword(token, NEW_PASSWORD, NEW_PASSWORD))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute(FlashMessage.SUCCESS, containsString("Senha redefinida")));

        assertThat(tokenRepository.count()).isZero();
        mockMvc.perform(formLogin("/login").user("joao").password(DEFAULT_PASSWORD)).andExpect(unauthenticated());
        mockMvc.perform(formLogin("/login").user("joao").password(NEW_PASSWORD)).andExpect(authenticated());

        mockMvc.perform(get("/reset-password").param("token", token))
                .andExpect(redirectedUrl("/forgot-password"))
                .andExpect(flash().attribute(FlashMessage.ERROR, containsString("Link inválido ou expirado")));
    }

    @Test
    void validatesNewPassword() throws Exception {
        String token = storeToken(Instant.now().plusSeconds(600));

        mockMvc.perform(resetPassword(token, "fraca", "fraca"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "password"));
        mockMvc.perform(resetPassword(token, NEW_PASSWORD, "outra1234"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "passwordConfirmed"));

        assertThat(tokenRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidAndExpiredTokens() throws Exception {
        mockMvc.perform(get("/reset-password").param("token", "inventado")).andExpect(redirectedUrl("/forgot-password"));
        mockMvc.perform(get("/reset-password")).andExpect(redirectedUrl("/forgot-password"));

        String expired = storeToken(Instant.now().minusSeconds(5));
        mockMvc.perform(get("/reset-password").param("token", expired)).andExpect(redirectedUrl("/forgot-password"));
        mockMvc.perform(resetPassword(expired, NEW_PASSWORD, NEW_PASSWORD)).andExpect(redirectedUrl("/forgot-password"));

        mockMvc.perform(formLogin("/login").user("joao").password(DEFAULT_PASSWORD)).andExpect(authenticated());
    }

    @Test
    void changesPasswordOfLoggedUser() throws Exception {
        mockMvc.perform(changePassword("errada123", NEW_PASSWORD))
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute(FlashMessage.ERROR, "Senha atual incorreta"));
        mockMvc.perform(changePassword(DEFAULT_PASSWORD, DEFAULT_PASSWORD))
                .andExpect(flash().attribute(FlashMessage.ERROR, "A nova senha deve ser diferente da atual"));

        mockMvc.perform(changePassword(DEFAULT_PASSWORD, NEW_PASSWORD))
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute(FlashMessage.SUCCESS, "Senha alterada com sucesso."));

        mockMvc.perform(formLogin("/login").user("joao").password(NEW_PASSWORD)).andExpect(authenticated());
    }

    @Test
    void changePasswordRequiresLogin() throws Exception {
        mockMvc.perform(get("/account/password")).andExpect(redirectedUrl("/login"));
    }

    private void requestReset(String email) throws Exception {
        mockMvc.perform(post("/forgot-password").with(csrf()).param("email", email))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute(FlashMessage.SUCCESS, containsString(GENERIC_MESSAGE)));
    }

    private MockHttpServletRequestBuilder resetPassword(String token, String password, String confirmation) {
        return post("/reset-password").with(csrf())
                .param("token", token)
                .param("password", password)
                .param("confirmPassword", confirmation);
    }

    private MockHttpServletRequestBuilder changePassword(String currentPassword, String newPassword) {
        return post("/account/password").with(csrf()).with(user(userRepository.findById(joao.getId()).orElseThrow()))
                .param("currentPassword", currentPassword)
                .param("password", newPassword)
                .param("confirmPassword", newPassword);
    }

    private String storeToken(Instant expiresAt) {
        String token = tokenGenerator.generate();
        tokenRepository.save(PasswordResetToken.builder()
                .tokenHash(tokenGenerator.hash(token))
                .userId(joao.getId())
                .expiresAt(expiresAt)
                .build());
        return token;
    }

    private String tokenFrom(SentMail mail) {
        String link = (String) mail.variables().get("resetLink");
        assertThat(link).startsWith("http://localhost:8080/reset-password?token=");
        return link.substring(link.indexOf('=') + 1);
    }
}

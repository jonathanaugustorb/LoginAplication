package com.example.LoginCompleto.auth;

import com.example.LoginCompleto.support.AbstractIntegrationTest;
import com.example.LoginCompleto.user.entity.User;
import com.example.LoginCompleto.user.entity.enums.UserRole;
import com.example.LoginCompleto.user.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

class AuthenticationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private IUserRepository userRepository;

    private User joao;

    @BeforeEach
    void setUp() {
        joao = createUser("joao", UserRole.SUPERVISOR);
    }

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/")).andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/items")).andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Esqueci minha senha")))
                .andExpect(content().string(containsString("Criar conta")));
    }

    @Test
    void authenticatedUserVisitingLoginGoesHome() throws Exception {
        mockMvc.perform(get("/login").with(user(joao))).andExpect(redirectedUrl("/"));
    }

    @Test
    void loginIsCaseInsensitiveAndLoadsRole() throws Exception {
        mockMvc.perform(formLogin("/login").user("JOAO").password(DEFAULT_PASSWORD))
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("joao").withRoles("SUPERVISOR"));
    }

    @Test
    void wrongPasswordAndUnknownUserGetTheSameError() throws Exception {
        mockMvc.perform(formLogin("/login").user("joao").password("errada123"))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
        mockMvc.perform(formLogin("/login").user("ninguem").password(DEFAULT_PASSWORD))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void disabledUserCannotLogin() throws Exception {
        joao.setEnabled(false);
        userRepository.save(joao);

        mockMvc.perform(formLogin("/login").user("joao").password(DEFAULT_PASSWORD))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void loginWithoutCsrfIsForbidden() throws Exception {
        mockMvc.perform(post("/login").param("username", "joao").param("password", DEFAULT_PASSWORD))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutRedirectsToLogin() throws Exception {
        mockMvc.perform(logout()).andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void sendsReferrerPolicyHeader() throws Exception {
        mockMvc.perform(get("/login")).andExpect(header().string("Referrer-Policy", "same-origin"));
    }
}

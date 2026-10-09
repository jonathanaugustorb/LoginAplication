package com.example.LoginCompleto.auth;

import com.example.LoginCompleto.common.web.FlashMessage;
import com.example.LoginCompleto.support.AbstractIntegrationTest;
import com.example.LoginCompleto.user.entity.User;
import com.example.LoginCompleto.user.entity.enums.UserRole;
import com.example.LoginCompleto.user.repository.IUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegistrationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private IUserRepository userRepository;

    @Test
    void showsRegistrationFormWithAllRoles() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(">Estoque<")))
                .andExpect(content().string(containsString(">Supervisor<")))
                .andExpect(content().string(containsString(">Gerente<")));
    }

    @Test
    void registersUserWithChosenRoleAndHashedPassword() throws Exception {
        mockMvc.perform(registration(" Ana.Souza ", "ANA@Mail.com", "GERENTE"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute(FlashMessage.SUCCESS, containsString("Conta criada")));

        User ana = userRepository.findByUsername("ana.souza").orElseThrow();
        assertThat(ana.getEmail()).isEqualTo("ana@mail.com");
        assertThat(ana.getRole()).isEqualTo(UserRole.GERENTE);
        assertThat(ana.getPassword()).startsWith("{bcrypt}").doesNotContain(DEFAULT_PASSWORD);
        assertThat(ana.isEnabled()).isTrue();
    }

    @Test
    void rejectsDuplicatedUsernameAndEmailKeepingTheForm() throws Exception {
        createUser("ana", UserRole.ESTOQUE);

        mockMvc.perform(registration("ANA", "outra@mail.com", "ESTOQUE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nome de usuário já está em uso")))
                .andExpect(content().string(containsString("value=\"outra@mail.com\"")));
        mockMvc.perform(registration("outra", "Ana@mail.com", "ESTOQUE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("E-mail já está em uso")));
    }

    @Test
    void validatesFields() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("name", "Pessoa Teste")
                        .param("username", "x")
                        .param("email", "nao-email")
                        .param("role", "")
                        .param("password", DEFAULT_PASSWORD)
                        .param("confirmPassword", "outra123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "username", "email", "role", "passwordConfirmed"));

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void showsFriendlyMessageForUnknownRole() throws Exception {
        mockMvc.perform(registration("bia", "bia@mail.com", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Selecione um perfil válido")))
                .andExpect(content().string(not(containsString("Failed to convert"))));
    }

    private MockHttpServletRequestBuilder registration(String username, String email, String role) {
        return post("/register").with(csrf())
                .param("name", "Pessoa Teste")
                .param("username", username)
                .param("email", email)
                .param("role", role)
                .param("password", DEFAULT_PASSWORD)
                .param("confirmPassword", DEFAULT_PASSWORD);
    }
}

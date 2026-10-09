package com.example.LoginCompleto.user.dto;

import com.example.LoginCompleto.common.validation.IPasswordConfirmation;
import com.example.LoginCompleto.common.validation.StrongPassword;
import com.example.LoginCompleto.user.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCreateDTO implements IPasswordConfirmation {

    @NotBlank(message = "Informe o nome")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String name;

    @NotBlank(message = "Informe o nome de usuário")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{3,30}$",
            message = "O usuário deve ter de 3 a 30 caracteres: letras, números, ponto, hífen ou sublinhado")
    private String username;

    @NotBlank(message = "Informe o e-mail")
    @Email(message = "Informe um e-mail válido")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
    private String email;

    @NotBlank(message = "Informe a senha")
    @StrongPassword
    private String password;

    @NotBlank(message = "Confirme a senha")
    private String confirmPassword;

    @NotNull(message = "Selecione o perfil")
    private UserRole role;
}

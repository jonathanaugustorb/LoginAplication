package com.example.LoginCompleto.auth.dto;

import com.example.LoginCompleto.common.validation.IPasswordConfirmation;
import com.example.LoginCompleto.common.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordDTO implements IPasswordConfirmation {

    @NotBlank(message = "Informe a senha atual")
    private String currentPassword;

    @NotBlank(message = "Informe a nova senha")
    @StrongPassword
    private String password;

    @NotBlank(message = "Confirme a nova senha")
    private String confirmPassword;
}

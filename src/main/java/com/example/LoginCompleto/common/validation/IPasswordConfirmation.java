package com.example.LoginCompleto.common.validation;

import jakarta.validation.constraints.AssertTrue;

import java.util.Objects;

public interface IPasswordConfirmation {

    String getPassword();

    String getConfirmPassword();

    @AssertTrue(message = "As senhas não conferem")
    default boolean isPasswordConfirmed() {
        return Objects.equals(getPassword(), getConfirmPassword());
    }
}

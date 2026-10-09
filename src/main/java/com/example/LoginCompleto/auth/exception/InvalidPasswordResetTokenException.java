package com.example.LoginCompleto.auth.exception;

import com.example.LoginCompleto.common.exception.BusinessException;

public class InvalidPasswordResetTokenException extends BusinessException {

    public InvalidPasswordResetTokenException() {
        super("Link inválido ou expirado. Solicite uma nova redefinição de senha.");
    }
}

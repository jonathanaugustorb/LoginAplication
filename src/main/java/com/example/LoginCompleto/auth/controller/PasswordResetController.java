package com.example.LoginCompleto.auth.controller;

import com.example.LoginCompleto.auth.dto.ForgotPasswordDTO;
import com.example.LoginCompleto.auth.dto.ResetPasswordDTO;
import com.example.LoginCompleto.auth.exception.InvalidPasswordResetTokenException;
import com.example.LoginCompleto.auth.service.PasswordResetService;
import com.example.LoginCompleto.common.web.FlashMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private static final String FORGOT_PASSWORD_VIEW = "auth/forgot-password";
    private static final String RESET_PASSWORD_VIEW = "auth/reset-password";
    private static final String REDIRECT_LOGIN = "redirect:/login";

    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String forgotPasswordForm(Model model) {
        model.addAttribute("form", new ForgotPasswordDTO());
        return FORGOT_PASSWORD_VIEW;
    }

    @PostMapping("/forgot-password")
    public String requestReset(@Valid @ModelAttribute("form") ForgotPasswordDTO form,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return FORGOT_PASSWORD_VIEW;

        passwordResetService.requestReset(form.getEmail());
        FlashMessage.success(redirectAttributes,
                "Se o e-mail estiver cadastrado, você receberá um link para redefinir a senha.");
        return REDIRECT_LOGIN;
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(required = false) String token, Model model) {
        passwordResetService.validateToken(token);

        var form = new ResetPasswordDTO();
        form.setToken(token);
        model.addAttribute("form", form);
        return RESET_PASSWORD_VIEW;
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("form") ResetPasswordDTO form,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return RESET_PASSWORD_VIEW;

        passwordResetService.resetPassword(form.getToken(), form.getPassword());
        FlashMessage.success(redirectAttributes, "Senha redefinida com sucesso. Entre com a nova senha.");
        return REDIRECT_LOGIN;
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public String handleInvalidToken(InvalidPasswordResetTokenException exception,
                                     RedirectAttributes redirectAttributes) {
        FlashMessage.error(redirectAttributes, exception.getMessage());
        return "redirect:/forgot-password";
    }
}

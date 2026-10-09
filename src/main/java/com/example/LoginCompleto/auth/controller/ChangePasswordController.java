package com.example.LoginCompleto.auth.controller;

import com.example.LoginCompleto.auth.dto.ChangePasswordDTO;
import com.example.LoginCompleto.common.web.FlashMessage;
import com.example.LoginCompleto.infra.security.UserSessionService;
import com.example.LoginCompleto.user.entity.User;
import com.example.LoginCompleto.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account/password")
@RequiredArgsConstructor
public class ChangePasswordController {

    private static final String CHANGE_PASSWORD_VIEW = "auth/change-password";

    private final UserService userService;
    private final UserSessionService userSessionService;

    @GetMapping
    public String changePasswordForm(Model model) {
        model.addAttribute("form", new ChangePasswordDTO());
        return CHANGE_PASSWORD_VIEW;
    }

    @PostMapping
    public String changePassword(@AuthenticationPrincipal User user,
                                 @Valid @ModelAttribute("form") ChangePasswordDTO form,
                                 BindingResult result,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return CHANGE_PASSWORD_VIEW;

        userService.changePassword(user.getId(), form.getCurrentPassword(), form.getPassword());
        userSessionService.expireSessionsExcept(user.getUsername(), session.getId());
        FlashMessage.success(redirectAttributes, "Senha alterada com sucesso.");
        return "redirect:/";
    }
}

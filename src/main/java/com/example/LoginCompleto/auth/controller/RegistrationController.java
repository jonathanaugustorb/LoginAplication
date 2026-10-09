package com.example.LoginCompleto.auth.controller;

import com.example.LoginCompleto.common.exception.BusinessException;
import com.example.LoginCompleto.common.web.FlashMessage;
import com.example.LoginCompleto.user.dto.UserCreateDTO;
import com.example.LoginCompleto.user.entity.enums.UserRole;
import com.example.LoginCompleto.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {

    private static final String REGISTER_VIEW = "auth/register";

    private final UserService userService;

    @ModelAttribute("roles")
    public UserRole[] roles() {
        return UserRole.values();
    }

    @GetMapping
    public String registerForm(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) return "redirect:/";

        model.addAttribute("form", new UserCreateDTO());
        return REGISTER_VIEW;
    }

    @PostMapping
    public String register(@Valid @ModelAttribute("form") UserCreateDTO form,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return REGISTER_VIEW;

        try {
            userService.register(form);
        } catch (BusinessException exception) {
            result.reject("business", exception.getMessage());
            return REGISTER_VIEW;
        }

        FlashMessage.success(redirectAttributes, "Conta criada com sucesso. Entre com seu usuário e senha.");
        return "redirect:/login";
    }
}

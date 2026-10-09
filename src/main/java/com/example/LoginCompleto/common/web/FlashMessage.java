package com.example.LoginCompleto.common.web;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public final class FlashMessage {

    public static final String SUCCESS = "successMessage";
    public static final String ERROR = "errorMessage";

    private FlashMessage() {
    }

    public static void success(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(SUCCESS, message);
    }

    public static void error(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(ERROR, message);
    }
}

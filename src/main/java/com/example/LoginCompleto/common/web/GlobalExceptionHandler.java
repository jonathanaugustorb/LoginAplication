package com.example.LoginCompleto.common.web;

import com.example.LoginCompleto.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String DEFAULT_REDIRECT_PATH = "/";

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException exception,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        FlashMessage.error(redirectAttributes, exception.getMessage());
        return "redirect:" + resolveRedirectPath(request);
    }

    private String resolveRedirectPath(HttpServletRequest request) {
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (referer == null) return DEFAULT_REDIRECT_PATH;

        try {
            URI uri = URI.create(referer);
            String path = uri.getRawPath();

            if (!request.getServerName().equalsIgnoreCase(uri.getHost())) return DEFAULT_REDIRECT_PATH;
            if (path == null || !path.startsWith("/") || path.startsWith("//")) return DEFAULT_REDIRECT_PATH;

            String contextPath = request.getContextPath();
            if (!contextPath.isEmpty() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }

            return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
        } catch (IllegalArgumentException exception) {
            return DEFAULT_REDIRECT_PATH;
        }
    }
}

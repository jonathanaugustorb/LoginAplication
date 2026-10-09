package com.example.LoginCompleto.infra.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.ZoneId;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @NotBlank String baseUrl,
        @NotNull ZoneId zoneId,
        @Valid @NotNull Mail mail,
        @Valid @NotNull PasswordReset passwordReset) {

    public record Mail(@NotBlank String from) {
    }

    public record PasswordReset(@NotNull Duration expiration, @NotNull Duration resendInterval) {
    }
}

package com.example.LoginCompleto.common.web;

import com.example.LoginCompleto.infra.config.AppProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Component("display")
public class DisplayFormatter {

    private static final String DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm";

    private final DateTimeFormatter dateTimeFormatter;

    public DisplayFormatter(AppProperties appProperties) {
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).withZone(appProperties.zoneId());
    }

    public String dateTime(Instant instant) {
        return instant == null ? "" : dateTimeFormatter.format(instant);
    }
}

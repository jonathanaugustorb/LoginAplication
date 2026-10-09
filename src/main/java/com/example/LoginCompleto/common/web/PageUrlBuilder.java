package com.example.LoginCompleto.common.web;

import org.springframework.data.domain.Sort;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class PageUrlBuilder {

    private static final String SORT_PARAM = "sort";

    private PageUrlBuilder() {
    }

    public static String build(String basePath, Map<String, Object> params, Sort sort) {
        var builder = UriComponentsBuilder.fromPath(basePath);

        params.forEach((name, value) -> {
            if (hasValue(value)) builder.queryParam(name, value);
        });
        sort.forEach(order -> builder.queryParam(SORT_PARAM, toSortParam(order)));

        return builder.encode().build().toUriString();
    }

    public static String sortParam(Sort sort) {
        return sort.stream()
                .map(PageUrlBuilder::toSortParam)
                .collect(Collectors.joining(";"));
    }

    private static String toSortParam(Sort.Order order) {
        return order.getProperty() + "," + order.getDirection().name().toLowerCase(Locale.ROOT);
    }

    private static boolean hasValue(Object value) {
        if (value == null || Boolean.FALSE.equals(value)) return false;
        return !(value instanceof String text) || !text.isBlank();
    }
}

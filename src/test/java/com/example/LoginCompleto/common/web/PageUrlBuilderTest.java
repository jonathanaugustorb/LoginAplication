package com.example.LoginCompleto.common.web;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PageUrlBuilderTest {

    @Test
    void keepsOnlyFilledParamsAndSort() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("search", "filtro óleo");
        params.put("category", null);
        params.put("blank", "  ");
        params.put("lowStock", false);
        params.put("active", true);

        String url = PageUrlBuilder.build("/items", params, Sort.by(Sort.Direction.DESC, "price"));

        assertThat(url).isEqualTo("/items?search=filtro%20%C3%B3leo&active=true&sort=price,desc");
    }

    @Test
    void returnsBasePathWhenNothingIsFilled() {
        assertThat(PageUrlBuilder.build("/items", Map.of(), Sort.unsorted())).isEqualTo("/items");
    }

    @Test
    void formatsSortParam() {
        assertThat(PageUrlBuilder.sortParam(Sort.by("name"))).isEqualTo("name,asc");
        assertThat(PageUrlBuilder.sortParam(Sort.unsorted())).isEmpty();
    }
}

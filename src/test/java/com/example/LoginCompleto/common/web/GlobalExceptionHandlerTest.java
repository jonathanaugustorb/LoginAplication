package com.example.LoginCompleto.common.web;

import com.example.LoginCompleto.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void redirectsBackToSameSitePageWithErrorMessage() {
        var attributes = new RedirectAttributesModelMap();

        String view = handler.handleBusinessException(new BusinessException("Erro de negócio"),
                requestWithReferer("http://localhost/items/1/edit?page=2"), attributes);

        assertThat(view).isEqualTo("redirect:/items/1/edit?page=2");
        assertThat(attributes.getFlashAttributes().get(FlashMessage.ERROR)).isEqualTo("Erro de negócio");
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://evil.com/x", "http://localhost//evil.com", "nao uma url", "javascript:alert(1)"})
    void neverRedirectsOutsideTheSite(String referer) {
        String view = handler.handleBusinessException(new BusinessException("Erro"),
                requestWithReferer(referer), new RedirectAttributesModelMap());

        assertThat(view).isEqualTo("redirect:/");
    }

    @Test
    void redirectsHomeWithoutReferer() {
        String view = handler.handleBusinessException(new BusinessException("Erro"),
                new MockHttpServletRequest(), new RedirectAttributesModelMap());

        assertThat(view).isEqualTo("redirect:/");
    }

    private MockHttpServletRequest requestWithReferer(String referer) {
        var request = new MockHttpServletRequest();
        request.addHeader("Referer", referer);
        return request;
    }
}

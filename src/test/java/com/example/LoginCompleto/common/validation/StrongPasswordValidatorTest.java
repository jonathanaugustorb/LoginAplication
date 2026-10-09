package com.example.LoginCompleto.common.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @ParameterizedTest
    @ValueSource(strings = {"senha123", "Abcdefg1", "çãoéíõ12"})
    void acceptsPasswordsWithLettersAndDigits(String password) {
        assertThat(validator.isValid(password, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"curta1", "somenteletras", "12345678", ""})
    void rejectsWeakPasswords(String password) {
        assertThat(validator.isValid(password, null)).isFalse();
    }

    @Test
    void enforcesMaximumLength() {
        assertThat(validator.isValid("a1".repeat(32), null)).isTrue();
        assertThat(validator.isValid("a1".repeat(32) + "a", null)).isFalse();
    }

    @Test
    void leavesNullToNotBlank() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}

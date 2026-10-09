package com.example.LoginCompleto.infra.security;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SecureTokenGeneratorTest {

    private final SecureTokenGenerator generator = new SecureTokenGenerator();

    @Test
    void generatesUrlSafeUniqueTokens() {
        Set<String> tokens = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            String token = generator.generate();
            assertThat(token).matches("^[A-Za-z0-9_-]{43}$");
            tokens.add(token);
        }

        assertThat(tokens).hasSize(1000);
    }

    @Test
    void hashIsDeterministicSha256Hex() {
        String hash = generator.hash("token");

        assertThat(hash).hasSize(64).matches("^[0-9a-f]+$");
        assertThat(generator.hash("token")).isEqualTo(hash);
        assertThat(generator.hash("outro")).isNotEqualTo(hash);
    }
}

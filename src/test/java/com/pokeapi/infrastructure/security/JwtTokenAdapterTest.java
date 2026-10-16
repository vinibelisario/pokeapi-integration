package com.pokeapi.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenAdapterTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long";

    @Test
    void generatedTokenValidatesToTheSameUsername() {
        JwtTokenAdapter adapter = new JwtTokenAdapter(SECRET, Duration.ofMinutes(60));

        String token = adapter.generate("alice");

        assertThat(adapter.validate(token)).contains("alice");
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtTokenAdapter adapter = new JwtTokenAdapter(SECRET, Duration.ofMinutes(60));

        String token = adapter.generate("alice") + "x";

        assertThat(adapter.validate(token)).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        JwtTokenAdapter adapter = new JwtTokenAdapter(SECRET, Duration.ofSeconds(-1));

        String token = adapter.generate("alice");

        assertThat(adapter.validate(token)).isEmpty();
    }
}
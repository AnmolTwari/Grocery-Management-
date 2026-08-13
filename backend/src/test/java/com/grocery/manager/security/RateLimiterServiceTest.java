package com.grocery.manager.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.grocery.manager.exception.RateLimitExceededException;

class RateLimiterServiceTest {

    private final RateLimiterService rateLimiterService = new RateLimiterService();

    @Test
    void allowsUpToMaxAttempts() {
        for (int i = 0; i < RateLimiterService.MAX_LOGIN_ATTEMPTS; i++) {
            rateLimiterService.record("key");
        }

        assertThatThrownBy(() -> rateLimiterService.check("key",
                RateLimiterService.MAX_LOGIN_ATTEMPTS, RateLimiterService.WINDOW))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void allowsRequestWhenUnderLimit() {
        rateLimiterService.record("key");

        assertThatCode(() -> rateLimiterService.check("key", 5, Duration.ofMinutes(15)))
                .doesNotThrowAnyException();
    }

    @Test
    void expiresOldAttemptsAfterWindow() throws InterruptedException {
        Duration window = Duration.ofMillis(20);
        for (int i = 0; i < 5; i++) {
            rateLimiterService.record("key");
        }

        assertThatThrownBy(() -> rateLimiterService.check("key", 5, window))
                .isInstanceOf(RateLimitExceededException.class);

        Thread.sleep(30);
        assertThatCode(() -> rateLimiterService.check("key", 5, window))
                .doesNotThrowAnyException();
    }

    @Test
    void clearResetsCounters() {
        for (int i = 0; i < 5; i++) {
            rateLimiterService.record("key");
        }
        rateLimiterService.clear("key");

        assertThatCode(() -> rateLimiterService.check("key", 5, Duration.ofMinutes(15)))
                .doesNotThrowAnyException();
    }
}
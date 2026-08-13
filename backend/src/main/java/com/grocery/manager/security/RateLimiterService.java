package com.grocery.manager.security;

import com.grocery.manager.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class RateLimiterService {

    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int MAX_REGISTER_ATTEMPTS = 5;
    public static final int MAX_PASSWORD_ATTEMPTS = 5;
    public static final Duration WINDOW = Duration.ofMinutes(15);

    private final Map<String, Deque<Long>> records = new ConcurrentHashMap<>();

    public void check(String key, int maxAttempts, Duration window) {
        Deque<Long> times = records.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        synchronized (times) {
            long cutoff = System.currentTimeMillis() - window.toMillis();
            times.removeIf(time -> time < cutoff);
            if (times.size() >= maxAttempts) {
                throw new RateLimitExceededException(window.toMinutes());
            }
        }
    }

    public void record(String key) {
        records.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>())
                .addLast(System.currentTimeMillis());
    }

    public void clear(String key) {
        records.remove(key);
    }

    public void checkLogin(HttpServletRequest request, String username) {
        check("login:user:" + username, MAX_LOGIN_ATTEMPTS, WINDOW);
        check("login:ip:" + clientIp(request), MAX_LOGIN_ATTEMPTS, WINDOW);
    }

    public void recordFailedLogin(HttpServletRequest request, String username) {
        record("login:user:" + username);
        record("login:ip:" + clientIp(request));
    }

    public void clearLoginAttempts(String username) {
        clear("login:user:" + username);
    }

    public void checkRegister(HttpServletRequest request) {
        check("register:ip:" + clientIp(request), MAX_REGISTER_ATTEMPTS, WINDOW);
    }

    public void recordRegisterAttempt(HttpServletRequest request) {
        record("register:ip:" + clientIp(request));
    }

    public void checkPasswordChange(Long userId) {
        check("password:user:" + userId, MAX_PASSWORD_ATTEMPTS, WINDOW);
    }

    public void recordFailedPasswordChange(Long userId) {
        record("password:user:" + userId);
    }

    public void clearPasswordAttempts(Long userId) {
        clear("password:user:" + userId);
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
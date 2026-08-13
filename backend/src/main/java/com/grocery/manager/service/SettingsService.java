package com.grocery.manager.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grocery.manager.entity.User;
import com.grocery.manager.repository.UserRepository;
import com.grocery.manager.security.CurrentUserService;
import com.grocery.manager.security.RateLimiterService;


@Service
public class SettingsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final RateLimiterService rateLimiterService;

    public SettingsService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService, RateLimiterService rateLimiterService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
        this.rateLimiterService = rateLimiterService;
    }

    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        User user = currentUserService.currentUser();
        rateLimiterService.checkPasswordChange(user.getId());
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            rateLimiterService.recordFailedPasswordChange(user.getId());
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the current one");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        rateLimiterService.clearPasswordAttempts(user.getId());
    }
}
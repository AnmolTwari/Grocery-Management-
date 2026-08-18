package com.shopmanager.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanager.entity.User;
import com.shopmanager.repository.UserRepository;
import com.shopmanager.security.CurrentUserService;
import com.shopmanager.security.RateLimiterService;
import com.shopmanager.util.EmailUtil;


@Service
public class SettingsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final RateLimiterService rateLimiterService;
    private final UserDetailsServiceImpl userDetailsService;

    public SettingsService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService, RateLimiterService rateLimiterService,
            UserDetailsServiceImpl userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
        this.rateLimiterService = rateLimiterService;
        this.userDetailsService = userDetailsService;
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
        userDetailsService.evictUser(user);
        rateLimiterService.clearPasswordAttempts(user.getId());
    }

    @Transactional
    public String changeEmail(String newEmail) {
        User user = currentUserService.currentUser();
        String email = EmailUtil.normalize(newEmail);
        if (email == null) {
            throw new IllegalArgumentException("Enter a valid email address");
        }
        if (email.equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("New email must be different from the current one");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered to another account");
        }
        String oldEmail = user.getEmail();
        user.setEmail(email);
        userRepository.save(user);
        userDetailsService.evict(oldEmail);
        userDetailsService.evictUser(user);
        return email;
    }
}
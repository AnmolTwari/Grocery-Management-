package com.shopmanager.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.shopmanager.entity.User;
import com.shopmanager.entity.UserRole;
import com.shopmanager.repository.UserRepository;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final int MIN_ADMIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USERNAME:}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String configured = adminUsername == null ? "" : adminUsername.trim();
        if (!configured.isEmpty()) {
            ensureConfiguredAdmin(configured);
            return;
        }

        if (userRepository.existsByRole(UserRole.ADMIN)) {
            return;
        }

        userRepository.findFirstByOrderByIdAsc().ifPresent(user -> {
            user.setRole(UserRole.ADMIN);
            userRepository.save(user);
            log.warn("No admin user exists. Promoted the first registered user '{}' to ADMIN. "
                    + "Set ADMIN_USERNAME in the backend .env to control this explicitly.",
                    user.getUsername());
        });
    }

    private void ensureConfiguredAdmin(String username) {
        Optional<User> existing = userRepository.findByUsername(username);
        String password = adminPassword == null ? "" : adminPassword.trim();

        if (existing.isPresent()) {
            User user = existing.get();
            boolean changed = false;

            if (user.getRole() != UserRole.ADMIN) {
                user.setRole(UserRole.ADMIN);
                changed = true;
                log.info("Promoted user '{}' to ADMIN (configured via ADMIN_USERNAME)", username);
            }

            if (!password.isEmpty()) {
                if (password.length() < MIN_ADMIN_PASSWORD_LENGTH) {
                    log.error("ADMIN_PASSWORD in the backend .env is shorter than {} characters. "
                            + "Password for '{}' was NOT updated.", MIN_ADMIN_PASSWORD_LENGTH,
                            username);
                } else if (user.getPassword() == null
                        || !passwordEncoder.matches(password, user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(password));
                    changed = true;
                    log.info("Updated password for admin user '{}' (configured via ADMIN_PASSWORD)",
                            username);
                }
            }

            if (changed) {
                userRepository.save(user);
            }
            return;
        }

        if (password.length() < MIN_ADMIN_PASSWORD_LENGTH) {
            log.error("Cannot create admin user '{}': app.admin.password is missing or shorter "
                    + "than {} characters. Set it in the backend .env file.", username,
                    MIN_ADMIN_PASSWORD_LENGTH);
            return;
        }

        User admin = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);
        log.info("Created admin user '{}' (configured via ADMIN_USERNAME)", username);
    }
}
package com.shopmanager.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.shopmanager.entity.User;
import com.shopmanager.entity.UserRole;
import com.shopmanager.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminBootstrapTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private AdminBootstrap bootstrap;

    private User admin;

    @BeforeEach
    void setUp() {
        bootstrap = new AdminBootstrap(userRepository, passwordEncoder);
        admin = User.builder().id(1L).username("admin").name("Admin").role(UserRole.ADMIN)
                .password("$2a$old-hash").enabled(true).build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void updatesPasswordWhenEnvPasswordDiffers() {
        ReflectionTestUtils.setField(bootstrap, "adminUsername", "admin");
        ReflectionTestUtils.setField(bootstrap, "adminPassword", "new-secret-123");
        when(passwordEncoder.matches("new-secret-123", "$2a$old-hash")).thenReturn(false);

        bootstrap.run(null);

        assertThat(admin.getPassword()).isNotEqualTo("$2a$old-hash");
        verify(passwordEncoder).encode("new-secret-123");
        verify(userRepository).save(admin);
    }

    @Test
    void leavesPasswordAloneWhenEnvPasswordMatches() {
        ReflectionTestUtils.setField(bootstrap, "adminUsername", "admin");
        ReflectionTestUtils.setField(bootstrap, "adminPassword", "same-secret-123");
        when(passwordEncoder.matches("same-secret-123", "$2a$old-hash")).thenReturn(true);

        bootstrap.run(null);

        assertThat(admin.getPassword()).isEqualTo("$2a$old-hash");
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void skipsPasswordUpdateWhenEnvPasswordTooShort() {
        ReflectionTestUtils.setField(bootstrap, "adminUsername", "admin");
        ReflectionTestUtils.setField(bootstrap, "adminPassword", "short");

        bootstrap.run(null);

        assertThat(admin.getPassword()).isEqualTo("$2a$old-hash");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void doesNothingWhenAdminMissingAndPasswordTooShort() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        ReflectionTestUtils.setField(bootstrap, "adminUsername", "admin");
        ReflectionTestUtils.setField(bootstrap, "adminPassword", "short");

        bootstrap.run(null);

        verify(userRepository, never()).save(any(User.class));
    }
}
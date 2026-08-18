package com.shopmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.shopmanager.entity.User;
import com.shopmanager.repository.UserRepository;
import com.shopmanager.security.CurrentUserService;
import com.shopmanager.security.RateLimiterService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SettingsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private SettingsService settingsService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder().username("owner").password("encoded-old").build();
        when(currentUserService.currentUser()).thenReturn(owner);
        when(passwordEncoder.matches("old-pass", "encoded-old")).thenReturn(true);
        when(passwordEncoder.matches("new-pass", "encoded-old")).thenReturn(false);
        when(passwordEncoder.encode("new-pass")).thenReturn("encoded-new");
    }

    @Test
    void changePasswordUpdatesWithEncodedValue() {
        settingsService.changePassword("old-pass", "new-pass");

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("new-pass");
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        when(passwordEncoder.matches("wrong", "encoded-old")).thenReturn(false);

        assertThatThrownBy(() -> settingsService.changePassword("wrong", "new-pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current password");
    }

    @Test
    void changeEmailNormalizesAndSaves() {
        String email = settingsService.changeEmail("  Shop@Example.com ");

        assertThat(email).isEqualTo("shop@example.com");
        assertThat(owner.getEmail()).isEqualTo("shop@example.com");
        verify(userRepository).save(owner);
        verify(userDetailsService).evictUser(owner);
    }

    @Test
    void changeEmailRejectsBlank() {
        assertThatThrownBy(() -> settingsService.changeEmail("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid email");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeEmailRejectsDuplicate() {
        when(userRepository.existsByEmailIgnoreCase("shop@example.com")).thenReturn(true);

        assertThatThrownBy(() -> settingsService.changeEmail("Shop@Example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeEmailRejectsSameEmail() {
        owner.setEmail("shop@example.com");

        assertThatThrownBy(() -> settingsService.changeEmail("shop@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different");
        verify(userRepository, never()).save(any(User.class));
    }
}
package com.grocery.manager.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.grocery.manager.dto.auth.AuthRequest;
import com.grocery.manager.dto.auth.AuthResponse;
import com.grocery.manager.entity.User;
import com.grocery.manager.exception.DuplicateResourceException;
import com.grocery.manager.repository.UserRepository;
import com.grocery.manager.security.JwtService;
import com.grocery.manager.service.UserDetailsServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthController authController;

    private final AuthRequest request = new AuthRequest();

    @Test
    void loginReturnsTokenAndType() {
        request.setUsername("shopkeeper");
        request.setPassword("shop123");

        User user = User.builder()
                .username("shopkeeper")
                .password("hashed")
                .enabled(true)
                .build();
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authController.login(request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUsername()).isEqualTo("shopkeeper");
    }

    @Test
    void registerCreatesUserAndReturnsToken() {
        request.setUsername("owner2");
        request.setPassword("secret123");

        when(userRepository.existsByUsername("owner2")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        User saved = User.builder()
                .username("owner2")
                .password("hashed")
                .enabled(true)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(userDetailsService.loadUserByUsername("owner2")).thenReturn(saved);
        when(jwtService.generateToken(saved)).thenReturn("jwt-token");

        AuthResponse response = authController.register(request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("owner2");
        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerRejectsDuplicateUsername() {
        request.setUsername("cashier");
        request.setPassword("secret123");

        when(userRepository.existsByUsername("cashier")).thenReturn(true);

        assertThatThrownBy(() -> authController.register(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any(User.class));
    }
}

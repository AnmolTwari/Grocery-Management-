package com.shopmanager.controller;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.shopmanager.dto.auth.AuthRequest;
import com.shopmanager.dto.auth.AuthResponse;
import com.shopmanager.entity.User;
import com.shopmanager.exception.DuplicateResourceException;
import com.shopmanager.repository.UserRepository;
import com.shopmanager.security.AuthCookieService;
import com.shopmanager.security.JwtService;
import com.shopmanager.security.RateLimiterService;

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
    private AuthCookieService authCookieService;

    @Mock
    private RateLimiterService rateLimiterService;

    @InjectMocks
    private AuthController authController;

    private final AuthRequest request = new AuthRequest();

    @Test
    void loginSetsHttpOnlyCookieAndReturnsUsername() {
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
        when(authCookieService.createTokenCookie("jwt-token"))
                .thenReturn(ResponseCookie.from(AuthCookieService.COOKIE_NAME, "jwt-token")
                        .httpOnly(true).build());

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthResponse body = authController.login(request, httpRequest, response).getBody();

        assertThat(body).isNotNull();
        assertThat(body.getUsername()).isEqualTo("shopkeeper");
        String cookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(cookie).contains("access_token=jwt-token").contains("HttpOnly");
        verify(rateLimiterService).clearLoginAttempts("shopkeeper");
    }

    @Test
    void loginRecordsFailedAttemptOnBadCredentials() {
        request.setUsername("shopkeeper");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> authController.login(request, httpRequest, response))
                .isInstanceOf(BadCredentialsException.class);
        verify(rateLimiterService).recordFailedLogin(httpRequest, "shopkeeper");
    }

    @Test
    void registerCreatesUserWithoutToken() {
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

        AuthResponse response = authController.register(request, new MockHttpServletRequest()).getBody();

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("owner2");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerRejectsDuplicateUsername() {
        request.setUsername("cashier");
        request.setPassword("secret123");

        when(userRepository.existsByUsername("cashier")).thenReturn(true);

        assertThatThrownBy(() -> authController.register(request, new MockHttpServletRequest()))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void logoutClearsCookie() {
        when(authCookieService.createLogoutCookie())
                .thenReturn(ResponseCookie.from(AuthCookieService.COOKIE_NAME, "").maxAge(0).build());

        MockHttpServletResponse response = new MockHttpServletResponse();
        authController.logout(response);

        String cookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(cookie).contains("access_token=");
    }
}
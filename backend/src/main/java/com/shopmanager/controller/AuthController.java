package com.shopmanager.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanager.dto.auth.AuthRequest;
import com.shopmanager.dto.auth.AuthResponse;
import com.shopmanager.entity.User;
import com.shopmanager.exception.DuplicateResourceException;
import com.shopmanager.repository.UserRepository;
import com.shopmanager.security.AuthCookieService;
import com.shopmanager.security.CurrentUserService;
import com.shopmanager.security.JwtService;
import com.shopmanager.security.RateLimiterService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthCookieService authCookieService;
    private final RateLimiterService rateLimiterService;
    private final CurrentUserService currentUserService;

    @GetMapping("/csrf")
    public Map<String, String> csrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return Map.of("token", token.getToken());
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me() {
        return ResponseEntity.ok(AuthResponse.builder()
                .username(currentUserService.currentUser().getUsername())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest, HttpServletResponse response) {
        rateLimiterService.checkLogin(httpRequest, request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createTokenCookie(token).toString());
            rateLimiterService.clearLoginAttempts(request.getUsername());
            return ResponseEntity.ok(AuthResponse.builder()
                    .username(userDetails.getUsername())
                    .build());
        } catch (BadCredentialsException e) {
            rateLimiterService.recordFailedLogin(httpRequest, request.getUsername());
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest) {
        rateLimiterService.checkRegister(httpRequest);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("USERNAME_TAKEN",
                    "Username is already taken", "username");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();

        userRepository.save(user);
        rateLimiterService.recordRegisterAttempt(httpRequest);

        return ResponseEntity.ok(AuthResponse.builder()
                .username(user.getUsername())
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.createLogoutCookie().toString());
        return ResponseEntity.noContent().build();
    }
}
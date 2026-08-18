package com.shopmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.security.core.userdetails.UserDetails;

import com.shopmanager.entity.User;
import com.shopmanager.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserDetailsServiceImpl userDetailsService;

    private User owner;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(userRepository);
        owner = User.builder()
                .username("owner")
                .email("owner@example.com")
                .password("hashed")
                .enabled(true)
                .build();
    }

    @Test
    void loadsByUsername() {
        when(userRepository.findByUsernameIgnoreCase("owner")).thenReturn(Optional.of(owner));

        UserDetails details = userDetailsService.loadUserByUsername("owner");

        assertThat(details.getUsername()).isEqualTo("owner");
        verify(userRepository, never()).findByEmailIgnoreCase("owner");
    }

    @Test
    void loadsByEmail() {
        when(userRepository.findByUsernameIgnoreCase("owner@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("owner@example.com")).thenReturn(Optional.of(owner));

        UserDetails details = userDetailsService.loadUserByUsername("owner@example.com");

        assertThat(details.getUsername()).isEqualTo("owner");
    }

    @Test
    void loadsByUsernameIgnoreCase() {
        when(userRepository.findByUsernameIgnoreCase("OWNER")).thenReturn(Optional.of(owner));

        UserDetails details = userDetailsService.loadUserByUsername("OWNER");

        assertThat(details.getUsername()).isEqualTo("owner");
    }

    @Test
    void throwsWhenNotFound() {
        when(userRepository.findByUsernameIgnoreCase("ghost")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(org.springframework.security.core.userdetails.UsernameNotFoundException.class);
    }

    @Test
    void evictUserClearsUsernameAndEmailKeys() {
        when(userRepository.findByUsernameIgnoreCase("owner")).thenReturn(Optional.of(owner));
        when(userRepository.findByEmailIgnoreCase("owner@example.com")).thenReturn(Optional.of(owner));
        userDetailsService.loadUserByUsername("owner");
        userDetailsService.loadUserByUsername("owner@example.com");

        userDetailsService.evictUser(owner);
        userDetailsService.loadUserByUsername("owner");

        verify(userRepository, org.mockito.Mockito.times(2)).findByUsernameIgnoreCase("owner");
        verify(userRepository, org.mockito.Mockito.times(1)).findByEmailIgnoreCase("owner@example.com");
    }
}
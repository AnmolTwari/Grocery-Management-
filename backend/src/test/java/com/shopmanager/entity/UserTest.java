package com.shopmanager.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void userRoleReturnsUserAuthority() {
        User user = User.builder().username("staff").build();

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void adminRoleReturnsAdminAuthority() {
        User user = User.builder().username("admin").role(UserRole.ADMIN).build();

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void nullRoleFallsBackToUserAuthority() {
        User user = new User();
        user.setRole(null);

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void enabledByDefault() {
        assertThat(new User().isEnabled()).isTrue();
    }
}

package com.shopmanager.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void authoritiesReturnOwnerRole() {
        User user = User.builder().username("owner").build();

        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_OWNER");
    }

    @Test
    void enabledByDefault() {
        assertThat(new User().isEnabled()).isTrue();
    }
}

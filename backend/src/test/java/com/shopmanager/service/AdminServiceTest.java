package com.shopmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.shopmanager.dto.admin.CreateUserRequest;
import com.shopmanager.dto.admin.UserResponse;
import com.shopmanager.dto.admin.UserUpdateRequest;
import com.shopmanager.dto.common.PageResponse;
import com.shopmanager.entity.User;
import com.shopmanager.entity.UserRole;
import com.shopmanager.exception.DuplicateResourceException;
import com.shopmanager.exception.ResourceNotFoundException;
import com.shopmanager.repository.ProductRepository;
import com.shopmanager.repository.SaleRepository;
import com.shopmanager.repository.UserRepository;
import com.shopmanager.security.CurrentUserService;

import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private User admin;
    private User staff;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1L).username("admin").role(UserRole.ADMIN).enabled(true).build();
        staff = User.builder().id(2L).username("staff").role(UserRole.USER).enabled(true).build();
        when(currentUserService.currentUser()).thenReturn(admin);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void listUsersReturnsPageOfUserResponses() {
        Pageable pageable = PageRequest.of(0, 20);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(java.util.List.of(admin, staff)));

        PageResponse<UserResponse> result = adminService.listUsers(pageable);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).username()).isEqualTo("admin");
        assertThat(result.content().get(0).role()).isEqualTo(UserRole.ADMIN);
        assertThat(result.content().get(1).username()).isEqualTo("staff");
    }

    @Test
    void updateUserPromotesToAdmin() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));

        UserResponse response = adminService.updateUser(2L,
                new UserUpdateRequest(UserRole.ADMIN, null, null));

        assertThat(response.role()).isEqualTo(UserRole.ADMIN);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserBlocksSelfDemotion() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(2L);

        assertThatThrownBy(() -> adminService.updateUser(1L,
                new UserUpdateRequest(UserRole.USER, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("demote");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserBlocksDemotingLastAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> adminService.updateUser(1L,
                new UserUpdateRequest(UserRole.USER, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("last admin");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserBlocksSelfDisable() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> adminService.updateUser(1L,
                new UserUpdateRequest(null, null, false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("disable");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserThrowsWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateUser(99L,
                new UserUpdateRequest(UserRole.ADMIN, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUserRemovesAccountWithNoData() {
        User empty = User.builder().id(3L).username("empty").role(UserRole.USER).enabled(true).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(empty));
        when(productRepository.countByOwner(empty)).thenReturn(0L);
        when(saleRepository.countByOwner(empty)).thenReturn(0L);

        adminService.deleteUser(3L);

        verify(userRepository).delete(empty);
        verify(userDetailsService).evictUser(empty);
    }

    @Test
    void deleteUserBlocksSelfDeletion() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(2L);

        assertThatThrownBy(() -> adminService.deleteUser(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own account");
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUserBlocksLastAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> adminService.deleteUser(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("last admin");
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUserBlocksAccountWithData() {
        User shop = User.builder().id(3L).username("shop").role(UserRole.USER).enabled(true).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(shop));
        when(productRepository.countByOwner(shop)).thenReturn(1L);
        when(saleRepository.countByOwner(shop)).thenReturn(0L);

        assertThatThrownBy(() -> adminService.deleteUser(3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("has products and/or sales");
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUserThrowsWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void analyticsAggregatesCounts() {
        when(userRepository.count()).thenReturn(5L);
        when(userRepository.countByEnabledTrue()).thenReturn(4L);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);
        when(productRepository.countByActiveTrue()).thenReturn(12L);
        when(productRepository.countLowStockAll()).thenReturn(2L);
        when(productRepository.countOutOfStockAll()).thenReturn(1L);
        when(saleRepository.count()).thenReturn(30L);
        when(saleRepository.countByCreatedAtGreaterThanEqual(any(LocalDateTime.class))).thenReturn(3L);
        when(saleRepository.sumTotalAmountAll()).thenReturn(new BigDecimal("1000.00"));
        when(saleRepository.sumTotalAmountSince(any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("150.00"));

        var analytics = adminService.analytics();

        assertThat(analytics.totalUsers()).isEqualTo(5L);
        assertThat(analytics.activeUsers()).isEqualTo(4L);
        assertThat(analytics.adminUsers()).isEqualTo(1L);
        assertThat(analytics.totalProducts()).isEqualTo(12L);
        assertThat(analytics.lowStockProducts()).isEqualTo(2L);
        assertThat(analytics.outOfStockProducts()).isEqualTo(1L);
        assertThat(analytics.totalSales()).isEqualTo(30L);
        assertThat(analytics.salesToday()).isEqualTo(3L);
        assertThat(analytics.totalRevenue()).isEqualByComparingTo("1000.00");
        assertThat(analytics.revenueToday()).isEqualByComparingTo("150.00");
    }

    @Test
    void createUserEncodesPasswordAndAssignsRole() {
        when(userRepository.existsByUsername("newstaff")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$encoded");

        UserResponse created = adminService.createUser(new CreateUserRequest(
                "newstaff", "newstaff@example.com", "secret123", "New Staff", UserRole.USER));

        assertThat(created.username()).isEqualTo("newstaff");
        assertThat(created.email()).isEqualTo("newstaff@example.com");
        assertThat(created.name()).isEqualTo("New Staff");
        assertThat(created.role()).isEqualTo(UserRole.USER);
        assertThat(created.enabled()).isTrue();
        verify(userRepository).save(any(User.class));
        verify(userDetailsService).evictUser(any(User.class));
    }

    @Test
    void createUserNormalizesBlankEmailToNull() {
        when(userRepository.existsByUsername("newstaff")).thenReturn(false);

        UserResponse created = adminService
                .createUser(new CreateUserRequest("newstaff", " ", "secret123", null, UserRole.USER));

        assertThat(created.email()).isNull();
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createUser(
                new CreateUserRequest("taken", null, "secret123", null, UserRole.USER)))
                .isInstanceOf(DuplicateResourceException.class)
                .extracting(ex -> ((DuplicateResourceException) ex).getErrorCode())
                .isEqualTo("USERNAME_TAKEN");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        when(userRepository.existsByUsername("newstaff")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("used@example.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createUser(
                new CreateUserRequest("newstaff", "used@example.com", "secret123", null, UserRole.ADMIN)))
                .isInstanceOf(DuplicateResourceException.class)
                .extracting(ex -> ((DuplicateResourceException) ex).getErrorCode())
                .isEqualTo("EMAIL_TAKEN");
        verify(userRepository, never()).save(any(User.class));
    }
}

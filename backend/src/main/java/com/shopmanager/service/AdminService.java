package com.shopmanager.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanager.dto.admin.AdminAnalyticsResponse;
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
import com.shopmanager.util.EmailUtil;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final CurrentUserService currentUserService;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, ProductRepository productRepository,
            SaleRepository saleRepository, CurrentUserService currentUserService,
            UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.currentUserService = currentUserService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable).map(UserResponse::from));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("USERNAME_TAKEN",
                    "Username is already taken", "username");
        }

        String email = EmailUtil.normalize(request.email());
        if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("EMAIL_TAKEN",
                    "Email is already registered", "email");
        }

        User user = User.builder()
                .username(request.username())
                .email(email)
                .name(normalizeName(request.name()))
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .enabled(true)
                .build();

        user = userRepository.save(user);
        userDetailsService.evictUser(user);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND",
                        "User not found: " + id));
        User current = currentUserService.currentUser();

        String email = request.email() != null ? EmailUtil.normalize(request.email()) : null;
        if (email != null && !email.equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalArgumentException(
                        "Email is already registered to another account");
            }
            user.setEmail(email);
        }

        if (request.role() != null && request.role() != user.getRole()) {
            if (request.role() == UserRole.USER && isLastAdmin(user)) {
                throw new IllegalArgumentException(
                        "Cannot demote the last admin. Promote another user to ADMIN first.");
            }
            if (request.role() == UserRole.USER && user.getId().equals(current.getId())) {
                throw new IllegalArgumentException("You cannot demote your own account.");
            }
            user.setRole(request.role());
        }

        if (request.enabled() != null && request.enabled() != user.isEnabled()) {
            if (!request.enabled() && user.getId().equals(current.getId())) {
                throw new IllegalArgumentException("You cannot disable your own account.");
            }
            if (!request.enabled() && isLastAdmin(user)) {
                throw new IllegalArgumentException(
                        "Cannot disable the last admin. Promote another user to ADMIN first.");
            }
            user.setEnabled(request.enabled());
        }

        UserResponse response = UserResponse.from(userRepository.save(user));
        userDetailsService.evictUser(user);
        return response;
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND",
                        "User not found: " + id));
        User current = currentUserService.currentUser();

        if (isLastAdmin(user)) {
            throw new IllegalArgumentException(
                    "Cannot delete the last admin. Promote another user to ADMIN first.");
        }
        if (user.getId().equals(current.getId())) {
            throw new IllegalArgumentException("You cannot delete your own account.");
        }
        if (productRepository.countByOwner(user) > 0 || saleRepository.countByOwner(user) > 0) {
            throw new IllegalArgumentException("Cannot delete '" + user.getUsername()
                    + "': the account has products and/or sales. Delete or transfer their data first.");
        }

        userRepository.delete(user);
        userDetailsService.evictUser(user);
    }

    @Transactional(readOnly = true)
    public AdminAnalyticsResponse analytics() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        return new AdminAnalyticsResponse(
                userRepository.count(),
                userRepository.countByEnabledTrue(),
                userRepository.countByRole(UserRole.ADMIN),
                productRepository.countByActiveTrue(),
                productRepository.countLowStockAll(),
                productRepository.countOutOfStockAll(),
                saleRepository.count(),
                saleRepository.countByCreatedAtGreaterThanEqual(startOfToday),
                saleRepository.sumTotalAmountAll(),
                saleRepository.sumTotalAmountSince(startOfToday));
    }

    private boolean isLastAdmin(User user) {
        return user.getRole() == UserRole.ADMIN && userRepository.countByRole(UserRole.ADMIN) == 1;
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

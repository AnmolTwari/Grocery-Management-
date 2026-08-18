package com.shopmanager.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanager.dto.admin.AdminAnalyticsResponse;
import com.shopmanager.dto.admin.CreateUserRequest;
import com.shopmanager.dto.admin.UserResponse;
import com.shopmanager.dto.admin.UserUpdateRequest;
import com.shopmanager.dto.common.PageResponse;
import com.shopmanager.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public PageResponse<UserResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(boundedPage, boundedSize,
                Sort.by(Sort.Direction.ASC, "id"));
        return adminService.listUsers(pageable);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return adminService.createUser(request);
    }

    @PatchMapping("/users/{id}")
    public UserResponse updateUser(@PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return adminService.updateUser(id, request);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
    }

    @GetMapping("/analytics")
    public AdminAnalyticsResponse analytics() {
        return adminService.analytics();
    }
}

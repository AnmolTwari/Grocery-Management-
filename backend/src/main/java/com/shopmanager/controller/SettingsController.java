package com.shopmanager.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanager.dto.settings.ChangeEmailRequest;
import com.shopmanager.dto.settings.ChangePasswordRequest;
import com.shopmanager.service.SettingsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        settingsService.changePassword(request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> changeEmail(@Valid @RequestBody ChangeEmailRequest request) {
        String email = settingsService.changeEmail(request.email());
        return ResponseEntity.ok(Map.of("email", email));
    }
}
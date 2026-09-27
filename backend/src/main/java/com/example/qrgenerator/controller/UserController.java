package com.example.qrgenerator.controller;

import com.example.qrgenerator.config.UserPrincipal;
import com.example.qrgenerator.dto.ApiResponse;
import com.example.qrgenerator.dto.PasswordUpdateRequest;
import com.example.qrgenerator.dto.ProfileUpdateRequest;
import com.example.qrgenerator.dto.UserResponse;
import com.example.qrgenerator.service.AuthService;
import com.example.qrgenerator.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                        @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(principal.getId(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse> updatePassword(@AuthenticationPrincipal UserPrincipal principal,
                                                        @Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.of(true, "Password updated successfully"));
    }
}

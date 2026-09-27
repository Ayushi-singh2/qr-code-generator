package com.example.qrgenerator.service;

import com.example.qrgenerator.dto.PasswordUpdateRequest;
import com.example.qrgenerator.dto.ProfileUpdateRequest;
import com.example.qrgenerator.dto.UserResponse;
import com.example.qrgenerator.entity.User;
import com.example.qrgenerator.exception.BadRequestException;
import com.example.qrgenerator.exception.ResourceNotFoundException;
import com.example.qrgenerator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = getUserOrThrow(userId);
        user.setFullName(request.getFullName().trim());
        User saved = userRepository.save(user);
        return toUserResponse(saved);
    }

    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("New password and confirm new password do not match");
        }
        User user = getUserOrThrow(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

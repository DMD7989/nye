package com.nye.backend.user.dto;

import com.nye.backend.user.Role;
import com.nye.backend.user.User;

import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String phone,
        String email,
        String language,
        Role role,
        boolean enabled,
        boolean phoneVerified,
        boolean notificationsEnabled,
        double notificationRadiusKm,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getLanguage(),
                user.getRole(),
                user.isEnabled(),
                user.isPhoneVerified(),
                user.isNotificationsEnabled(),
                user.getNotificationRadiusKm(),
                user.getCreatedAt()
        );
    }
}

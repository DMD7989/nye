package com.nye.backend.auth.dto;

import com.nye.backend.user.Role;

public record AuthResponse(
        String token,
        Long userId,
        String fullName,
        Role role,
        boolean phoneVerified
) {
}

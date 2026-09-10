package com.nye.backend.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateFcmTokenRequest(
        @NotBlank(message = "Le jeton FCM est obligatoire") String token
) {
}

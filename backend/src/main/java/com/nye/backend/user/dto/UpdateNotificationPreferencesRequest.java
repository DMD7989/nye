package com.nye.backend.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateNotificationPreferencesRequest(
        @NotNull(message = "Le paramètre 'enabled' est obligatoire") Boolean enabled,
        @Positive(message = "Le rayon doit être positif") Double radiusKm
) {
}

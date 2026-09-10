package com.nye.backend.user.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateLocationRequest(
        @NotNull(message = "La latitude est obligatoire") Double latitude,
        @NotNull(message = "La longitude est obligatoire") Double longitude
) {
}

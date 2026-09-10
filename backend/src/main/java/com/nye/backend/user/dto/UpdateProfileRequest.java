package com.nye.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank(message = "Le nom complet est obligatoire") String fullName,
        @Email(message = "Email invalide") String email,
        @NotBlank(message = "La langue est obligatoire") String language
) {
}

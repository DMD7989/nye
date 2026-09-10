package com.nye.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Le téléphone est obligatoire") String phone,
        @NotBlank(message = "Le mot de passe est obligatoire") String password
) {
}

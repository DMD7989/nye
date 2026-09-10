package com.nye.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpRequest(
        @NotBlank(message = "Le téléphone est obligatoire") String phone
) {
}

package com.nye.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpVerifyRequest(
        @NotBlank(message = "Le téléphone est obligatoire") String phone,
        @NotBlank(message = "Le code est obligatoire") String code
) {
}

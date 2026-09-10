package com.nye.backend.alert.dto;

import jakarta.validation.constraints.NotBlank;

public record CloseAlertRequest(
        @NotBlank(message = "Une note de résolution est obligatoire") String resolutionNote
) {
}

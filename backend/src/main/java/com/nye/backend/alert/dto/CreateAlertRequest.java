package com.nye.backend.alert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateAlertRequest(
        @NotBlank(message = "Le nom de la personne disparue est obligatoire") String missingPersonName,
        @Positive(message = "L'âge doit être positif") Integer missingPersonAge,
        @NotNull(message = "Merci de préciser si la personne disparue est mineure") Boolean missingPersonIsMinor,
        @NotBlank(message = "La description est obligatoire") String description,
        @NotBlank(message = "La photo est obligatoire") String photoUrl,
        @NotBlank(message = "Le contact est obligatoire") String contactPhone,
        @NotNull(message = "La latitude est obligatoire") Double latitude,
        @NotNull(message = "La longitude est obligatoire") Double longitude,
        String address
) {
}

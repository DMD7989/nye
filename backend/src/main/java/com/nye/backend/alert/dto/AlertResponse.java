package com.nye.backend.alert.dto;

import com.nye.backend.alert.Alert;
import com.nye.backend.alert.AlertStatus;

import java.time.Instant;

public record AlertResponse(
        Long id,
        String missingPersonName,
        Integer missingPersonAge,
        String description,
        String photoUrl,
        boolean photoPending,
        String contactPhone,
        Double latitude,
        Double longitude,
        String address,
        AlertStatus status,
        Long createdById,
        String createdByName,
        Instant createdAt,
        Instant validatedAt,
        Instant resolvedAt,
        String resolutionNote,
        boolean moderationFlagged,
        String moderationReason,
        String rejectionReason,
        Double distanceKm,
        boolean restricted
) {
    /** Vue complète, réservée aux administrateurs et à l'auteur de l'alerte. */
    public static AlertResponse from(Alert alert) {
        return from(alert, null);
    }

    public static AlertResponse from(Alert alert, Double distanceKm) {
        return new AlertResponse(
                alert.getId(),
                alert.getMissingPersonName(),
                alert.getMissingPersonAge(),
                alert.getDescription(),
                alert.getPhotoUrl(),
                false,
                alert.getContactPhone(),
                alert.getLatitude(),
                alert.getLongitude(),
                alert.getAddress(),
                alert.getStatus(),
                alert.getCreatedBy().getId(),
                alert.getCreatedBy().getFullName(),
                alert.getCreatedAt(),
                alert.getValidatedAt(),
                alert.getResolvedAt(),
                alert.getResolutionNote(),
                alert.isModerationFlagged(),
                alert.getModerationReason(),
                alert.getRejectionReason(),
                distanceKm,
                false
        );
    }

    /**
     * Vue publique restreinte pour une alerte pas encore validée (ou rejetée) par un administrateur
     * (cahier des charges §13.1) : position floutée (~1 km), photo masquée, nom raccourci si la
     * personne disparue est mineure, et aucune information de modération (réservée aux admins).
     */
    public static AlertResponse restricted(Alert alert, Double blurredLat, Double blurredLon, Double distanceKm) {
        String displayName = alert.isMissingPersonIsMinor()
                ? maskName(alert.getMissingPersonName())
                : alert.getMissingPersonName();

        return new AlertResponse(
                alert.getId(),
                displayName,
                alert.getMissingPersonAge(),
                alert.getDescription(),
                null,
                true,
                alert.getContactPhone(),
                blurredLat,
                blurredLon,
                null,
                alert.getStatus(),
                alert.getCreatedBy().getId(),
                null,
                alert.getCreatedAt(),
                alert.getValidatedAt(),
                alert.getResolvedAt(),
                alert.getResolutionNote(),
                false,
                null,
                null,
                distanceKm,
                true
        );
    }

    private static String maskName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return fullName;
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0];
        }
        return parts[0] + " " + parts[parts.length - 1].charAt(0) + ".";
    }
}

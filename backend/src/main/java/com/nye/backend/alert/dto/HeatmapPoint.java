package com.nye.backend.alert.dto;

import com.nye.backend.alert.Alert;
import com.nye.backend.alert.AlertStatus;

public record HeatmapPoint(
        Long alertId,
        Double latitude,
        Double longitude,
        AlertStatus status
) {
    public static HeatmapPoint from(Alert alert) {
        return new HeatmapPoint(alert.getId(), alert.getLatitude(), alert.getLongitude(), alert.getStatus());
    }
}

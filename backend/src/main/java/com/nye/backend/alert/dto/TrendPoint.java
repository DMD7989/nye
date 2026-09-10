package com.nye.backend.alert.dto;

import java.time.LocalDate;

public record TrendPoint(
        LocalDate date,
        long alertsCreated,
        long alertsResolved
) {
}

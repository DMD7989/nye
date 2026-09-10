package com.nye.backend.alert.dto;

public record DashboardSummaryResponse(
        long totalAlerts,
        long pendingAlerts,
        long activeAlerts,
        long resolvedAlerts,
        long rejectedAlerts,
        long flaggedPendingAlerts,
        Double closureRatePercent,
        Double averageResolutionHours,
        long newAlertsLast7Days,
        long totalUsers,
        long verifiedUsers,
        long suspendedUsers
) {
}

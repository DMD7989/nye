package com.nye.backend.alert;

import com.nye.backend.alert.dto.DashboardSummaryResponse;
import com.nye.backend.alert.dto.HeatmapPoint;
import com.nye.backend.alert.dto.TrendPoint;
import com.nye.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
public class AlertAnalyticsService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    public DashboardSummaryResponse getSummary() {
        long pending = alertRepository.countByStatus(AlertStatus.PENDING);
        long active = alertRepository.countByStatus(AlertStatus.ACTIVE);
        long resolved = alertRepository.countByStatus(AlertStatus.RESOLVED);
        long rejected = alertRepository.countByStatus(AlertStatus.REJECTED);
        long total = pending + active + resolved + rejected;
        long flaggedPending = alertRepository.countByStatusAndModerationFlagged(AlertStatus.PENDING, true);

        // Taux de clôture calculé sur les alertes déjà validées (actives + résolues) :
        // une alerte encore en attente n'a pas encore été examinée, elle ne compte pas contre le taux.
        long validated = active + resolved;
        Double closureRate = validated == 0 ? null : (resolved * 100.0) / validated;

        OptionalDouble avgResolutionMinutes = alertRepository.findByStatus(AlertStatus.RESOLVED).stream()
                .filter(a -> a.getResolvedAt() != null)
                .mapToLong(a -> Duration.between(a.getCreatedAt(), a.getResolvedAt()).toMinutes())
                .average();
        Double avgResolutionHours = avgResolutionMinutes.isPresent()
                ? avgResolutionMinutes.getAsDouble() / 60.0
                : null;

        long newLast7Days = alertRepository.countByCreatedAtAfter(Instant.now().minus(Duration.ofDays(7)));

        long totalUsers = userRepository.count();
        long verifiedUsers = userRepository.countByPhoneVerified(true);
        long suspendedUsers = userRepository.countByEnabled(false);

        return new DashboardSummaryResponse(
                total, pending, active, resolved, rejected, flaggedPending,
                closureRate, avgResolutionHours, newLast7Days,
                totalUsers, verifiedUsers, suspendedUsers
        );
    }

    /**
     * Nombre d'alertes créées / résolues par jour, sur les {@code days} derniers jours.
     */
    public List<TrendPoint> getTrends(int days) {
        Instant since = Instant.now().minus(Duration.ofDays(days));
        List<Alert> alerts = alertRepository.findRelevantForTrends(since);

        Map<LocalDate, long[]> byDay = new LinkedHashMap<>();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        for (int i = days - 1; i >= 0; i--) {
            byDay.put(today.minusDays(i), new long[]{0, 0});
        }

        for (Alert alert : alerts) {
            LocalDate createdDay = LocalDate.ofInstant(alert.getCreatedAt(), ZoneOffset.UTC);
            long[] createdBucket = byDay.get(createdDay);
            if (createdBucket != null) {
                createdBucket[0]++;
            }
            if (alert.getResolvedAt() != null) {
                LocalDate resolvedDay = LocalDate.ofInstant(alert.getResolvedAt(), ZoneOffset.UTC);
                long[] resolvedBucket = byDay.get(resolvedDay);
                if (resolvedBucket != null) {
                    resolvedBucket[1]++;
                }
            }
        }

        return byDay.entrySet().stream()
                .map(e -> new TrendPoint(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparing(TrendPoint::date))
                .toList();
    }

    /**
     * Points géolocalisés pour la heatmap admin. Vue interne (non restreinte) :
     * réservée aux endpoints /api/admin/**.
     */
    public List<HeatmapPoint> getHeatmap(AlertStatus statusFilter) {
        List<Alert> alerts = statusFilter == null
                ? alertRepository.findAll()
                : alertRepository.findByStatus(statusFilter);
        return alerts.stream().map(HeatmapPoint::from).toList();
    }
}

package com.nye.backend.alert;

import com.nye.backend.alert.dto.DashboardSummaryResponse;
import com.nye.backend.alert.dto.TrendPoint;
import com.nye.backend.user.Role;
import com.nye.backend.user.User;
import com.nye.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertAnalyticsServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private UserRepository userRepository;

    private AlertAnalyticsService analyticsService;

    private User dummyAuthor() {
        return User.builder().id(1L).fullName("X").phone("+22370000000")
                .passwordHash("h").role(Role.USER).build();
    }

    private Alert resolvedAlert(Instant createdAt, Instant resolvedAt) {
        return Alert.builder()
                .missingPersonName("X").description("d").photoUrl("p").contactPhone("c")
                .latitude(12.0).longitude(-8.0).status(AlertStatus.RESOLVED)
                .createdBy(dummyAuthor()).createdAt(createdAt).resolvedAt(resolvedAt)
                .build();
    }

    @Test
    void getSummary_computesClosureRateAndAverageResolutionHours() {
        analyticsService = new AlertAnalyticsService(alertRepository, userRepository);

        when(alertRepository.countByStatus(AlertStatus.PENDING)).thenReturn(1L);
        when(alertRepository.countByStatus(AlertStatus.ACTIVE)).thenReturn(2L);
        when(alertRepository.countByStatus(AlertStatus.RESOLVED)).thenReturn(3L);
        when(alertRepository.countByStatus(AlertStatus.REJECTED)).thenReturn(1L);
        when(alertRepository.countByStatusAndModerationFlagged(AlertStatus.PENDING, true)).thenReturn(1L);

        Instant now = Instant.now();
        List<Alert> resolved = List.of(
                resolvedAlert(now.minus(Duration.ofHours(2)), now),
                resolvedAlert(now.minus(Duration.ofHours(4)), now)
        );
        when(alertRepository.findByStatus(AlertStatus.RESOLVED)).thenReturn(resolved);
        when(alertRepository.countByCreatedAtAfter(any())).thenReturn(4L);

        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByPhoneVerified(true)).thenReturn(8L);
        when(userRepository.countByEnabled(false)).thenReturn(1L);

        DashboardSummaryResponse summary = analyticsService.getSummary();

        assertThat(summary.totalAlerts()).isEqualTo(7);
        assertThat(summary.pendingAlerts()).isEqualTo(1);
        assertThat(summary.activeAlerts()).isEqualTo(2);
        assertThat(summary.resolvedAlerts()).isEqualTo(3);
        assertThat(summary.rejectedAlerts()).isEqualTo(1);
        assertThat(summary.flaggedPendingAlerts()).isEqualTo(1);
        // 3 resolved / (2 active + 3 resolved) = 60%
        assertThat(summary.closureRatePercent()).isCloseTo(60.0, within(0.01));
        // Average of 2h and 4h = 3h
        assertThat(summary.averageResolutionHours()).isCloseTo(3.0, within(0.01));
        assertThat(summary.totalUsers()).isEqualTo(10);
        assertThat(summary.verifiedUsers()).isEqualTo(8);
        assertThat(summary.suspendedUsers()).isEqualTo(1);
    }

    @Test
    void getSummary_closureRateAndAverageAreNull_whenNoAlertsHaveBeenValidated() {
        analyticsService = new AlertAnalyticsService(alertRepository, userRepository);

        when(alertRepository.countByStatus(AlertStatus.PENDING)).thenReturn(5L);
        when(alertRepository.countByStatus(AlertStatus.ACTIVE)).thenReturn(0L);
        when(alertRepository.countByStatus(AlertStatus.RESOLVED)).thenReturn(0L);
        when(alertRepository.countByStatus(AlertStatus.REJECTED)).thenReturn(0L);
        when(alertRepository.countByStatusAndModerationFlagged(AlertStatus.PENDING, true)).thenReturn(0L);
        when(alertRepository.findByStatus(AlertStatus.RESOLVED)).thenReturn(List.of());
        when(alertRepository.countByCreatedAtAfter(any())).thenReturn(5L);
        when(userRepository.count()).thenReturn(5L);
        when(userRepository.countByPhoneVerified(true)).thenReturn(1L);
        when(userRepository.countByEnabled(false)).thenReturn(0L);

        DashboardSummaryResponse summary = analyticsService.getSummary();

        assertThat(summary.closureRatePercent()).isNull();
        assertThat(summary.averageResolutionHours()).isNull();
    }

    @Test
    void getTrends_placesActivityInTodaysBucket() {
        analyticsService = new AlertAnalyticsService(alertRepository, userRepository);

        Instant now = Instant.now();
        Alert createdToday = Alert.builder()
                .missingPersonName("X").description("d").photoUrl("p").contactPhone("c")
                .latitude(12.0).longitude(-8.0).status(AlertStatus.ACTIVE)
                .createdBy(dummyAuthor()).createdAt(now)
                .build();
        when(alertRepository.findRelevantForTrends(any())).thenReturn(List.of(createdToday));

        List<TrendPoint> trends = analyticsService.getTrends(3);

        assertThat(trends).hasSize(3);
        TrendPoint todayPoint = trends.get(trends.size() - 1);
        assertThat(todayPoint.date()).isEqualTo(LocalDate.now(ZoneOffset.UTC));
        assertThat(todayPoint.alertsCreated()).isEqualTo(1);
        assertThat(todayPoint.alertsResolved()).isEqualTo(0);
        // Earlier days in the window have no activity.
        assertThat(trends.get(0).alertsCreated()).isEqualTo(0);
    }
}

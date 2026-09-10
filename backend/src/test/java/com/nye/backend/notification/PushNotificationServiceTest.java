package com.nye.backend.notification;

import com.nye.backend.alert.Alert;
import com.nye.backend.alert.AlertStatus;
import com.nye.backend.user.Role;
import com.nye.backend.user.User;
import com.nye.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FcmConfig fcmConfig;

    private PushNotificationService service;

    private User author;

    private Alert alertNear(double lat, double lon) {
        author = User.builder().id(1L).fullName("Auteur").phone("+22370000001")
                .passwordHash("h").role(Role.USER).build();
        return Alert.builder()
                .id(1L).missingPersonName("Test").description("d").photoUrl("p").contactPhone("c")
                .status(AlertStatus.ACTIVE).latitude(lat).longitude(lon).createdBy(author)
                .build();
    }

    private User userAt(long id, double lat, double lon, double radiusKm) {
        return User.builder().id(id).fullName("U" + id).phone("+2237000" + id)
                .passwordHash("h").role(Role.USER)
                .lastKnownLatitude(lat).lastKnownLongitude(lon)
                .notificationRadiusKm(radiusKm).notificationsEnabled(true)
                .fcmToken("token-" + id)
                .build();
    }

    @Test
    void filterRecipients_excludesUsersBeyondTheirOwnRadius() {
        service = new PushNotificationService(userRepository, fcmConfig);
        Alert alert = alertNear(12.6392, -8.0029);

        User near = userAt(2L, 12.6400, -8.0000, 10.0);   // within radius
        User far = userAt(3L, 13.9000, -8.5000, 10.0);    // far beyond radius

        List<User> recipients = service.filterRecipients(List.of(near, far), alert);

        assertThat(recipients).containsExactly(near);
    }

    @Test
    void filterRecipients_excludesTheAlertAuthor() {
        service = new PushNotificationService(userRepository, fcmConfig);
        Alert alert = alertNear(12.6392, -8.0029);

        User authorAsCandidate = userAt(author.getId(), 12.6392, -8.0029, 10.0);
        User someoneElse = userAt(2L, 12.6400, -8.0000, 10.0);

        List<User> recipients = service.filterRecipients(List.of(authorAsCandidate, someoneElse), alert);

        assertThat(recipients).containsExactly(someoneElse);
    }

    @Test
    void filterRecipients_returnsEmpty_whenNoCandidates() {
        service = new PushNotificationService(userRepository, fcmConfig);
        Alert alert = alertNear(12.6392, -8.0029);

        List<User> recipients = service.filterRecipients(List.of(), alert);

        assertThat(recipients).isEmpty();
    }

    @Test
    void notifyNearbyUsers_doesNotThrow_whenFcmIsNotInitialized() {
        service = new PushNotificationService(userRepository, fcmConfig);
        Alert alert = alertNear(12.6392, -8.0029);
        User near = userAt(2L, 12.6400, -8.0000, 10.0);

        when(userRepository.findByNotificationsEnabledTrueAndFcmTokenIsNotNullAndLastKnownLatitudeIsNotNullAndLastKnownLongitudeIsNotNull())
                .thenReturn(List.of(near));
        when(fcmConfig.isInitialized()).thenReturn(false);

        service.notifyNearbyUsers(alert);
        // Falls back to the simulated/logged path instead of calling FirebaseMessaging — no exception expected.
    }
}

package com.nye.backend.alert;

import com.nye.backend.alert.dto.AlertResponse;
import com.nye.backend.alert.dto.CreateAlertRequest;
import com.nye.backend.alert.dto.RejectAlertRequest;
import com.nye.backend.common.PhoneNotVerifiedException;
import com.nye.backend.common.ResourceNotFoundException;
import com.nye.backend.moderation.ModerationResult;
import com.nye.backend.moderation.ModerationService;
import com.nye.backend.notification.PushNotificationService;
import com.nye.backend.user.Role;
import com.nye.backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private ModerationService moderationService;

    private AlertService alertService;

    private User author;
    private User otherUser;
    private User admin;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(alertRepository, pushNotificationService, moderationService);

        author = User.builder().id(1L).fullName("Auteur").phone("+22370000001")
                .passwordHash("h").role(Role.USER).phoneVerified(true).build();
        otherUser = User.builder().id(2L).fullName("Autre").phone("+22370000002")
                .passwordHash("h").role(Role.USER).phoneVerified(true).build();
        admin = User.builder().id(3L).fullName("Admin").phone("+22300000000")
                .passwordHash("h").role(Role.ADMIN).phoneVerified(true).build();
    }

    private CreateAlertRequest sampleRequest() {
        return new CreateAlertRequest(
                "Personne Disparue", 10, false, "Description", "https://example.com/p.jpg",
                "+22370000001", 12.6392, -8.0029, "Bamako"
        );
    }

    private Alert pendingAlert() {
        return Alert.builder()
                .id(1L)
                .missingPersonName("Personne Disparue")
                .missingPersonIsMinor(false)
                .description("Description")
                .photoUrl("https://example.com/p.jpg")
                .contactPhone("+22370000001")
                .latitude(12.6392)
                .longitude(-8.0029)
                .status(AlertStatus.PENDING)
                .createdBy(author)
                .createdAt(java.time.Instant.now())
                .build();
    }

    @Test
    void create_throwsPhoneNotVerified_whenAuthorNotVerified() {
        author.setPhoneVerified(false);

        assertThatThrownBy(() -> alertService.create(sampleRequest(), author))
                .isInstanceOf(PhoneNotVerifiedException.class);

        verifyNoInteractions(alertRepository);
    }

    @Test
    void create_savesAlertAsPending_whenAuthorVerified() {
        when(moderationService.moderateAlert(any(), any(), any())).thenReturn(ModerationResult.clear());
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertResponse response = alertService.create(sampleRequest(), author);

        assertThat(response.status()).isEqualTo(AlertStatus.PENDING);
        assertThat(response.restricted()).isFalse();
        assertThat(response.latitude()).isEqualTo(12.6392);
        assertThat(response.moderationFlagged()).isFalse();
    }

    @Test
    void create_flagsAlert_whenModerationDetectsSuspiciousContent() {
        when(moderationService.moderateAlert(any(), any(), any()))
                .thenReturn(ModerationResult.flagged("Contenu suspect détecté automatiquement"));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertResponse response = alertService.create(sampleRequest(), author);

        assertThat(response.moderationFlagged()).isTrue();
        assertThat(response.moderationReason()).isEqualTo("Contenu suspect détecté automatiquement");
        // Flagged content is still created for admin review — not silently blocked.
        assertThat(response.status()).isEqualTo(AlertStatus.PENDING);
    }

    @Test
    void getById_returnsRestrictedView_forAnonymousViewer_onPendingAlert() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(pendingAlert()));

        AlertResponse response = alertService.getById(1L, null);

        assertThat(response.restricted()).isTrue();
        assertThat(response.photoUrl()).isNull();
        assertThat(response.photoPending()).isTrue();
        // Blurred coordinates must not equal the exact stored value's un-rounded precision loss check:
        assertThat(response.latitude()).isEqualTo(GeoUtils.blur(12.6392));
    }

    @Test
    void getById_returnsRestrictedView_forUnrelatedUser_onPendingAlert() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(pendingAlert()));

        AlertResponse response = alertService.getById(1L, otherUser);

        assertThat(response.restricted()).isTrue();
    }

    @Test
    void getById_returnsFullView_forAuthor_onPendingAlert() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(pendingAlert()));

        AlertResponse response = alertService.getById(1L, author);

        assertThat(response.restricted()).isFalse();
        assertThat(response.photoUrl()).isEqualTo("https://example.com/p.jpg");
        assertThat(response.latitude()).isEqualTo(12.6392);
    }

    @Test
    void getById_returnsFullView_forAdmin_onPendingAlert() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(pendingAlert()));

        AlertResponse response = alertService.getById(1L, admin);

        assertThat(response.restricted()).isFalse();
    }

    @Test
    void getById_returnsFullView_forAnyone_onceAlertIsActive() {
        Alert active = pendingAlert();
        active.setStatus(AlertStatus.ACTIVE);
        when(alertRepository.findById(1L)).thenReturn(Optional.of(active));

        AlertResponse response = alertService.getById(1L, null);

        assertThat(response.restricted()).isFalse();
        assertThat(response.photoUrl()).isEqualTo("https://example.com/p.jpg");
    }

    @Test
    void getById_throws_whenAlertDoesNotExist() {
        when(alertRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.getById(99L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void validate_transitionsToActive_andNotifiesNearbyUsers() {
        Alert alert = pendingAlert();
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertResponse response = alertService.validate(1L);

        assertThat(response.status()).isEqualTo(AlertStatus.ACTIVE);
        verify(pushNotificationService).notifyNearbyUsers(any(Alert.class));
    }

    @Test
    void validate_stillSucceeds_whenNotificationDispatchFails() {
        Alert alert = pendingAlert();
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("FCM down")).when(pushNotificationService).notifyNearbyUsers(any());

        AlertResponse response = alertService.validate(1L);

        assertThat(response.status()).isEqualTo(AlertStatus.ACTIVE);
    }

    @Test
    void close_setsResolvedStatus_withNote() {
        Alert alert = pendingAlert();
        alert.setStatus(AlertStatus.ACTIVE);
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertResponse response = alertService.close(1L, new com.nye.backend.alert.dto.CloseAlertRequest("Retrouvé"));

        assertThat(response.status()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(response.resolutionNote()).isEqualTo("Retrouvé");
    }

    @Test
    void reject_setsRejectedStatus_withReason() {
        Alert alert = pendingAlert();
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertResponse response = alertService.reject(1L, new RejectAlertRequest("Contenu frauduleux"));

        assertThat(response.status()).isEqualTo(AlertStatus.REJECTED);
        assertThat(response.rejectionReason()).isEqualTo("Contenu frauduleux");
    }

    @Test
    void getById_returnsRestrictedView_forAnonymous_onRejectedAlert() {
        Alert alert = pendingAlert();
        alert.setStatus(AlertStatus.REJECTED);
        alert.setRejectionReason("Canular");
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));

        AlertResponse response = alertService.getById(1L, null);

        assertThat(response.restricted()).isTrue();
    }

    @Test
    void getById_returnsFullView_forAuthor_onRejectedAlert() {
        Alert alert = pendingAlert();
        alert.setStatus(AlertStatus.REJECTED);
        alert.setRejectionReason("Canular");
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));

        AlertResponse response = alertService.getById(1L, author);

        assertThat(response.restricted()).isFalse();
        assertThat(response.rejectionReason()).isEqualTo("Canular");
    }
}

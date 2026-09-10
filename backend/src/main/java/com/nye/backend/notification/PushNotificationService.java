package com.nye.backend.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.nye.backend.alert.Alert;
import com.nye.backend.alert.GeoUtils;
import com.nye.backend.user.User;
import com.nye.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Notifications push géolocalisées (Nyé-F5) : dès qu'une alerte est validée, les utilisateurs
 * à proximité (position auto-déclarée périodiquement par l'app, pas de suivi continu — voir
 * cahier des charges §13.1) et ayant activé les notifications sont prévenus via FCM.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final UserRepository userRepository;
    private final FcmConfig fcmConfig;

    public void notifyNearbyUsers(Alert alert) {
        List<User> candidates = userRepository
                .findByNotificationsEnabledTrueAndFcmTokenIsNotNullAndLastKnownLatitudeIsNotNullAndLastKnownLongitudeIsNotNull();

        List<User> recipients = filterRecipients(candidates, alert);

        if (recipients.isEmpty()) {
            log.info("[FCM] Alerte #{} validée : aucun utilisateur à proximité avec notifications activées.",
                    alert.getId());
            return;
        }

        String title = "Alerte disparition à proximité";
        String body = "Une personne a été signalée disparue près de chez vous : " + alert.getMissingPersonName();

        for (User user : recipients) {
            send(user.getFcmToken(), title, body, alert.getId());
        }
    }

    /**
     * Parmi les candidats déjà filtrés côté base (notifications activées, jeton FCM et position
     * connus), exclut l'auteur de l'alerte et ceux situés au-delà de leur propre rayon préféré.
     */
    List<User> filterRecipients(List<User> candidates, Alert alert) {
        return candidates.stream()
                .filter(user -> !user.getId().equals(alert.getCreatedBy().getId()))
                .filter(user -> {
                    double distance = GeoUtils.distanceKm(
                            alert.getLatitude(), alert.getLongitude(),
                            user.getLastKnownLatitude(), user.getLastKnownLongitude());
                    return distance <= user.getNotificationRadiusKm();
                })
                .toList();
    }

    private void send(String fcmToken, String title, String body, Long alertId) {
        if (!fcmConfig.isInitialized()) {
            log.info("[FCM] (simulé) Notification à {} : \"{}\" — {} (alerte #{})",
                    fcmToken, title, body, alertId);
            return;
        }

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putData("alertId", String.valueOf(alertId))
                .putData("type", "ALERT_VALIDATED")
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException ex) {
            log.error("[FCM] Échec de l'envoi de la notification pour l'alerte #{} : {}", alertId, ex.getMessage());
        }
    }
}

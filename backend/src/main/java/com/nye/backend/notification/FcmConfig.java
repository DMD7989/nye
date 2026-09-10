package com.nye.backend.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Initialise Firebase Admin SDK si des identifiants sont fournis. En leur absence
 * (environnement de dev par défaut), l'envoi de notifications retombe sur un mode
 * simulé (voir PushNotificationService), comme pour l'envoi des OTP par SMS.
 */
@Configuration
@Slf4j
public class FcmConfig {

    private boolean initialized = false;

    @Value("${nye.fcm.credentials-path:}")
    private String credentialsPath;

    @PostConstruct
    public void init() {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            log.warn("[FCM] Aucun identifiant configuré (nye.fcm.credentials-path) — "
                    + "les notifications push seront simulées (journalisées) au lieu d'être envoyées.");
            return;
        }
        try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            initialized = true;
            log.info("[FCM] Firebase Admin SDK initialisé avec succès.");
        } catch (IOException ex) {
            log.error("[FCM] Impossible d'initialiser Firebase avec les identifiants fournis ({}) : {}",
                    credentialsPath, ex.getMessage());
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}

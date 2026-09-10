package com.nye.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    long countByPhoneVerified(boolean phoneVerified);
    long countByEnabled(boolean enabled);

    /**
     * Candidats à une notification géolocalisée : notifications activées, jeton FCM enregistré,
     * et une position connue (mise à jour périodiquement par l'app, pas un suivi continu).
     */
    List<User> findByNotificationsEnabledTrueAndFcmTokenIsNotNullAndLastKnownLatitudeIsNotNullAndLastKnownLongitudeIsNotNull();
}

package com.nye.backend.alert;

import com.nye.backend.alert.dto.AlertResponse;
import com.nye.backend.alert.dto.CloseAlertRequest;
import com.nye.backend.alert.dto.CreateAlertRequest;
import com.nye.backend.alert.dto.RejectAlertRequest;
import com.nye.backend.common.PhoneNotVerifiedException;
import com.nye.backend.common.ResourceNotFoundException;
import com.nye.backend.moderation.ModerationResult;
import com.nye.backend.moderation.ModerationService;
import com.nye.backend.notification.PushNotificationService;
import com.nye.backend.user.Role;
import com.nye.backend.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final PushNotificationService pushNotificationService;
    private final ModerationService moderationService;

    public AlertResponse create(CreateAlertRequest request, User author) {
        if (!author.isPhoneVerified()) {
            throw new PhoneNotVerifiedException(
                    "Vérifiez votre numéro de téléphone (code OTP) avant de publier une alerte");
        }

        ModerationResult moderation = moderationService.moderateAlert(
                request.missingPersonName(), request.description(), request.photoUrl());

        Alert alert = Alert.builder()
                .missingPersonName(request.missingPersonName())
                .missingPersonAge(request.missingPersonAge())
                .missingPersonIsMinor(Boolean.TRUE.equals(request.missingPersonIsMinor()))
                .description(request.description())
                .photoUrl(request.photoUrl())
                .contactPhone(request.contactPhone())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .address(request.address())
                .status(AlertStatus.PENDING)
                .createdBy(author)
                .moderationFlagged(moderation.flagged())
                .moderationReason(moderation.reason())
                .build();
        alert = alertRepository.save(alert);
        // L'auteur voit toujours sa propre alerte en clair, y compris avant validation.
        return AlertResponse.from(alert);
    }

    public AlertResponse getById(Long id, User viewer) {
        Alert alert = findOrThrow(id);
        if (canSeeFullDetails(alert, viewer)) {
            return AlertResponse.from(alert);
        }
        return toRestrictedResponse(alert, null, null);
    }

    /**
     * Alertes visibles publiquement (en attente de validation + actives), triées par
     * proximité quand une position est fournie. Une alerte en attente de validation est
     * renvoyée avec une position floutée et une identité restreinte si mineure (§13.1),
     * sauf pour son auteur ou un administrateur.
     */
    public List<AlertResponse> listPublic(Double lat, Double lon, Double radiusKm, User viewer) {
        List<Alert> alerts = alertRepository.findByStatusIn(List.of(AlertStatus.PENDING, AlertStatus.ACTIVE));

        return alerts.stream()
                .map(alert -> toResponseForViewer(alert, viewer, lat, lon))
                .filter(r -> radiusKm == null || r.distanceKm() == null || r.distanceKm() <= radiusKm)
                .sorted(Comparator.comparing(AlertResponse::distanceKm, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<AlertResponse> listResolved() {
        return alertRepository.findByStatus(AlertStatus.RESOLVED).stream()
                .map(AlertResponse::from)
                .toList();
    }

    public List<AlertResponse> listByStatus(AlertStatus status) {
        return alertRepository.findByStatus(status).stream()
                .map(AlertResponse::from)
                .toList();
    }

    public AlertResponse validate(Long id) {
        Alert alert = findOrThrow(id);
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setValidatedAt(Instant.now());
        alert = alertRepository.save(alert);

        try {
            pushNotificationService.notifyNearbyUsers(alert);
        } catch (Exception ex) {
            // Un échec d'envoi de notification ne doit jamais invalider la validation de l'alerte.
            log.error("Échec de l'envoi des notifications pour l'alerte #{} : {}", alert.getId(), ex.getMessage());
        }

        return AlertResponse.from(alert);
    }

    public AlertResponse close(Long id, CloseAlertRequest request) {
        Alert alert = findOrThrow(id);
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(Instant.now());
        alert.setResolutionNote(request.resolutionNote());
        return AlertResponse.from(alertRepository.save(alert));
    }

    /**
     * Rejette une alerte en attente (contenu frauduleux/sensible détecté par la modération,
     * ou canular manifeste) — §13.6. Le motif est communiqué au déclarant, qui garde accès au
     * détail complet de sa propre alerte malgré le statut non public.
     */
    public AlertResponse reject(Long id, RejectAlertRequest request) {
        Alert alert = findOrThrow(id);
        alert.setStatus(AlertStatus.REJECTED);
        alert.setRejectionReason(request.reason());
        return AlertResponse.from(alertRepository.save(alert));
    }

    private AlertResponse toResponseForViewer(Alert alert, User viewer, Double lat, Double lon) {
        boolean fullDetails = canSeeFullDetails(alert, viewer);

        double effectiveLat = fullDetails ? alert.getLatitude() : GeoUtils.blur(alert.getLatitude());
        double effectiveLon = fullDetails ? alert.getLongitude() : GeoUtils.blur(alert.getLongitude());

        Double distance = (lat == null || lon == null)
                ? null
                : GeoUtils.distanceKm(lat, lon, effectiveLat, effectiveLon);

        if (fullDetails) {
            return AlertResponse.from(alert, distance);
        }
        return toRestrictedResponse(alert, effectiveLat, effectiveLon, distance);
    }

    private AlertResponse toRestrictedResponse(Alert alert, Double blurredLat, Double blurredLon) {
        return toRestrictedResponse(alert, blurredLat, blurredLon, null);
    }

    private AlertResponse toRestrictedResponse(Alert alert, Double blurredLat, Double blurredLon, Double distanceKm) {
        double lat = blurredLat != null ? blurredLat : GeoUtils.blur(alert.getLatitude());
        double lon = blurredLon != null ? blurredLon : GeoUtils.blur(alert.getLongitude());
        return AlertResponse.restricted(alert, lat, lon, distanceKm);
    }

    /**
     * Une alerte en attente ou rejetée n'est visible en clair que par son auteur ou un
     * administrateur (cahier des charges §13.1/§13.2/§13.6). Une alerte active ou résolue,
     * elle, est déjà publique.
     */
    private boolean canSeeFullDetails(Alert alert, User viewer) {
        if (alert.getStatus() != AlertStatus.PENDING && alert.getStatus() != AlertStatus.REJECTED) {
            return true;
        }
        if (viewer == null) {
            return false;
        }
        return viewer.getRole() == Role.ADMIN || viewer.getId().equals(alert.getCreatedBy().getId());
    }

    private Alert findOrThrow(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte introuvable : " + id));
    }
}

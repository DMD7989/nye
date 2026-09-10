package com.nye.backend.moderation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Modération de contenu (Nyé-A5, cahier des charges §13.6) : chaque alerte est filtrée avant
 * validation par un administrateur, pour repérer les contenus frauduleux ou manifestement
 * abusifs (demandes d'argent déguisées en avis de recherche, propos haineux...).
 *
 * <p>Le filtrage textuel ci-dessous est une liste de mots-clés volontairement simple, pensée
 * comme un premier filtre grossier — <b>pas</b> comme une solution de modération complète.
 * La vérification de la photo est un stub qui ne signale jamais rien : brancher un vrai service
 * de modération d'image (AWS Rekognition, Google Vision SafeSearch, Azure Content Moderator...)
 * avant la mise en production, à l'image des stubs déjà en place pour l'OTP et FCM.</p>
 */
@Service
@Slf4j
public class ModerationService {

    private static final List<String> SUSPICIOUS_PATTERNS = List.of(
            "envoyer de l'argent", "envoyez de l'argent", "envoie de l'argent",
            "paiement urgent", "virement immediat", "transfert orange money",
            "transfert moov money", "western union", "rancon", "demande de rancon",
            "code de retrait", "cliquez sur ce lien", "cliquer sur ce lien"
    );

    public ModerationResult moderateAlert(String missingPersonName, String description, String photoUrl) {
        ModerationResult textResult = moderateText(missingPersonName, description);
        if (textResult.flagged()) {
            return textResult;
        }
        return moderatePhoto(photoUrl);
    }

    private ModerationResult moderateText(String... texts) {
        for (String text : texts) {
            if (text == null || text.isBlank()) {
                continue;
            }
            String normalized = normalize(text);
            for (String pattern : SUSPICIOUS_PATTERNS) {
                if (normalized.contains(pattern)) {
                    log.info("[Modération] Contenu signalé (motif : \"{}\")", pattern);
                    return ModerationResult.flagged(
                            "Contenu suspect détecté automatiquement (expression correspondant à \""
                                    + pattern + "\") — à vérifier avant validation.");
                }
            }
        }
        return ModerationResult.clear();
    }

    /**
     * TODO: brancher un vrai service de modération d'image avant la mise en production.
     * En l'absence d'intégration réelle, on ne peut pas analyser le contenu de la photo :
     * ce stub ne signale donc jamais rien, il ne fait que documenter le point d'extension.
     */
    private ModerationResult moderatePhoto(String photoUrl) {
        return ModerationResult.clear();
    }

    private String normalize(String text) {
        String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.FRENCH);
    }
}

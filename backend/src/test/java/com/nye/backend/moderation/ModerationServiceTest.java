package com.nye.backend.moderation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModerationServiceTest {

    private final ModerationService moderationService = new ModerationService();

    @Test
    void moderateAlert_isClear_forOrdinaryContent() {
        ModerationResult result = moderationService.moderateAlert(
                "Ibrahim Konate", "Vu pour la dernière fois près du marché central.",
                "https://example.com/photo.jpg");

        assertThat(result.flagged()).isFalse();
        assertThat(result.reason()).isNull();
    }

    @Test
    void moderateAlert_flags_moneyRequestPattern_inDescription() {
        ModerationResult result = moderationService.moderateAlert(
                "X", "Merci d'envoyer de l'argent pour aider à financer les recherches.",
                "https://example.com/photo.jpg");

        assertThat(result.flagged()).isTrue();
        assertThat(result.reason()).isNotBlank();
    }

    @Test
    void moderateAlert_flags_regardlessOfAccentsOrCase() {
        ModerationResult result = moderationService.moderateAlert(
                "X", "VIREMENT IMMÉDIAT exigé avant toute information.",
                "https://example.com/photo.jpg");

        assertThat(result.flagged()).isTrue();
    }

    @Test
    void moderateAlert_flags_suspiciousLinkPattern_inName() {
        // Even an unusual field like the name is screened, not just the description.
        ModerationResult result = moderationService.moderateAlert(
                "Cliquez sur ce lien pour plus d'infos", "Description normale.",
                "https://example.com/photo.jpg");

        assertThat(result.flagged()).isTrue();
    }

    @Test
    void moderateAlert_neverFlagsPhotoContent_inTheDevStub() {
        // Documented limitation: without a real image-moderation provider wired in,
        // the photo check can never flag anything on its own.
        ModerationResult result = moderationService.moderateAlert(
                "X", "Description normale et anodine.", "https://example.com/anything-at-all.jpg");

        assertThat(result.flagged()).isFalse();
    }
}

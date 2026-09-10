package com.nye.backend.moderation;

public record ModerationResult(boolean flagged, String reason) {

    public static ModerationResult clear() {
        return new ModerationResult(false, null);
    }

    public static ModerationResult flagged(String reason) {
        return new ModerationResult(true, reason);
    }
}

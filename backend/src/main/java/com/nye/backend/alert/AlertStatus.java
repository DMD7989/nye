package com.nye.backend.alert;

public enum AlertStatus {
    PENDING,
    ACTIVE,
    RESOLVED,
    /** Rejetée par un administrateur (contenu frauduleux/sensible ou canular) — voir §13.6. */
    REJECTED
}

package com.nye.backend.alert;

import com.nye.backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String missingPersonName;

    @Column
    private Integer missingPersonAge;

    @Column(nullable = false)
    @Builder.Default
    private boolean missingPersonIsMinor = false;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String photoUrl;

    @Column(nullable = false)
    private String contactPhone;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AlertStatus status = AlertStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant validatedAt;

    @Column
    private Instant resolvedAt;

    @Column(length = 1000)
    private String resolutionNote;

    @Column(nullable = false)
    @Builder.Default
    private boolean moderationFlagged = false;

    @Column(length = 1000)
    private String moderationReason;

    @Column(length = 1000)
    private String rejectionReason;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}

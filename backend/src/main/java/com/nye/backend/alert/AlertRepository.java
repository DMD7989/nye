package com.nye.backend.alert;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatus(AlertStatus status);
    List<Alert> findByStatusIn(Collection<AlertStatus> statuses);
    Page<Alert> findByStatus(AlertStatus status, Pageable pageable);
    long countByStatus(AlertStatus status);
    long countByCreatedAtAfter(Instant since);
    long countByStatusAndModerationFlagged(AlertStatus status, boolean moderationFlagged);

    @Query("select a from Alert a where a.createdAt >= :since or a.resolvedAt >= :since")
    List<Alert> findRelevantForTrends(@Param("since") Instant since);
}

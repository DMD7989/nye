package com.nye.backend.alert;

import com.nye.backend.alert.dto.AlertResponse;
import com.nye.backend.alert.dto.CreateAlertRequest;
import com.nye.backend.security.SecurityUser;
import com.nye.backend.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Alertes")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<AlertResponse>> listPublic(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double radiusKm,
            @AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.ok(alertService.listPublic(lat, lon, radiusKm, userOf(principal)));
    }

    @GetMapping("/resolved")
    public ResponseEntity<List<AlertResponse>> listResolved() {
        return ResponseEntity.ok(alertService.listResolved());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.ok(alertService.getById(id, userOf(principal)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AlertResponse> create(
            @Valid @RequestBody CreateAlertRequest request,
            @AuthenticationPrincipal SecurityUser principal) {
        AlertResponse response = alertService.create(request, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private User userOf(SecurityUser principal) {
        return principal == null ? null : principal.getUser();
    }
}

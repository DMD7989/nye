package com.nye.backend.alert;

import com.nye.backend.alert.dto.AlertResponse;
import com.nye.backend.alert.dto.CloseAlertRequest;
import com.nye.backend.alert.dto.RejectAlertRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/alerts")
@RequiredArgsConstructor
@Tag(name = "Administration - Alertes")
public class AdminAlertController {

    private final AlertService alertService;

    @GetMapping
    public List<AlertResponse> list(@RequestParam(required = false) AlertStatus status) {
        if (status == null) {
            return alertService.listByStatus(AlertStatus.PENDING);
        }
        return alertService.listByStatus(status);
    }

    @PatchMapping("/{id}/validate")
    public AlertResponse validate(@PathVariable Long id) {
        return alertService.validate(id);
    }

    @PatchMapping("/{id}/close")
    public AlertResponse close(@PathVariable Long id, @Valid @RequestBody CloseAlertRequest request) {
        return alertService.close(id, request);
    }

    @PatchMapping("/{id}/reject")
    public AlertResponse reject(@PathVariable Long id, @Valid @RequestBody RejectAlertRequest request) {
        return alertService.reject(id, request);
    }
}

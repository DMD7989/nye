package com.nye.backend.alert;

import com.nye.backend.alert.dto.DashboardSummaryResponse;
import com.nye.backend.alert.dto.HeatmapPoint;
import com.nye.backend.alert.dto.TrendPoint;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Administration - Dashboard analytique")
public class AdminDashboardController {

    private final AlertAnalyticsService analyticsService;

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        return analyticsService.getSummary();
    }

    @GetMapping("/trends")
    public List<TrendPoint> trends(@RequestParam(defaultValue = "30") int days) {
        int clamped = Math.max(1, Math.min(days, 365));
        return analyticsService.getTrends(clamped);
    }

    @GetMapping("/heatmap")
    public List<HeatmapPoint> heatmap(@RequestParam(required = false) AlertStatus status) {
        return analyticsService.getHeatmap(status);
    }
}

import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import * as L from 'leaflet';

import { DashboardService } from '../../core/services/dashboard.service';
import { AlertStatus } from '../../core/models/alert.model';

const STATUS_COLORS: Record<AlertStatus, string> = {
  PENDING: '#f59e0b',
  ACTIVE: '#b3261e',
  RESOLVED: '#2e7d32',
  REJECTED: '#757575',
};

// Bamako, par défaut.
const DEFAULT_CENTER: L.LatLngExpression = [12.6392, -8.0029];

@Component({
  selector: 'app-heatmap',
  standalone: true,
  imports: [FormsModule, MatFormFieldModule, MatSelectModule],
  templateUrl: './heatmap.component.html',
  styleUrl: './heatmap.component.scss',
})
export class HeatmapComponent implements AfterViewInit, OnDestroy {
  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef<HTMLDivElement>;

  readonly statusOptions: { value: AlertStatus | ''; label: string }[] = [
    { value: '', label: 'Tous les statuts' },
    { value: 'PENDING', label: 'En attente' },
    { value: 'ACTIVE', label: 'Active' },
    { value: 'RESOLVED', label: 'Résolue' },
    { value: 'REJECTED', label: 'Rejetée' },
  ];

  selectedStatus: AlertStatus | '' = '';

  private map: L.Map | null = null;
  private markersLayer: L.LayerGroup | null = null;

  constructor(private dashboardService: DashboardService) {}

  ngAfterViewInit(): void {
    this.map = L.map(this.mapContainer.nativeElement).setView(DEFAULT_CENTER, 12);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19,
    }).addTo(this.map);
    this.markersLayer = L.layerGroup().addTo(this.map);

    this.loadPoints();
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  loadPoints(): void {
    this.dashboardService.getHeatmap(this.selectedStatus).subscribe((points) => {
      this.markersLayer?.clearLayers();
      for (const point of points) {
        L.circleMarker([point.latitude, point.longitude], {
          radius: 8,
          color: STATUS_COLORS[point.status],
          fillColor: STATUS_COLORS[point.status],
          fillOpacity: 0.7,
        })
          .bindPopup(`Alerte #${point.alertId} — ${point.status}`)
          .addTo(this.markersLayer!);
      }
    });
  }
}

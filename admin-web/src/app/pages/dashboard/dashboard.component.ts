import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';

import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardSummary, TrendPoint } from '../../core/models/dashboard.model';

interface StatCard {
  label: string;
  value: string;
  icon: string;
  accent?: 'warning' | 'danger' | 'success';
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatProgressSpinnerModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  loading = signal(true);
  error = signal<string | null>(null);
  summary = signal<DashboardSummary | null>(null);
  trends = signal<TrendPoint[]>([]);
  cards = signal<StatCard[]>([]);

  // Dimensions du graphique SVG des tendances.
  readonly chartWidth = 720;
  readonly chartHeight = 220;
  private readonly chartPadding = 28;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.dashboardService.getSummary().subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.cards.set(this.buildCards(summary));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger le tableau de bord.');
        this.loading.set(false);
      },
    });

    this.dashboardService.getTrends(30).subscribe({
      next: (trends) => this.trends.set(trends),
    });
  }

  private buildCards(s: DashboardSummary): StatCard[] {
    return [
      { label: 'Alertes en attente', value: `${s.pendingAlerts}`, icon: 'hourglass_top', accent: s.pendingAlerts > 0 ? 'warning' : undefined },
      { label: 'Dont signalées (modération)', value: `${s.flaggedPendingAlerts}`, icon: 'flag', accent: s.flaggedPendingAlerts > 0 ? 'danger' : undefined },
      { label: 'Alertes actives', value: `${s.activeAlerts}`, icon: 'campaign' },
      { label: 'Alertes résolues', value: `${s.resolvedAlerts}`, icon: 'task_alt', accent: 'success' },
      { label: 'Alertes rejetées', value: `${s.rejectedAlerts}`, icon: 'block' },
      { label: 'Taux de clôture', value: s.closureRatePercent !== null ? `${s.closureRatePercent.toFixed(0)} %` : '—', icon: 'insights' },
      { label: 'Temps moyen de résolution', value: s.averageResolutionHours !== null ? `${s.averageResolutionHours.toFixed(1)} h` : '—', icon: 'schedule' },
      { label: 'Nouvelles alertes (7 j)', value: `${s.newAlertsLast7Days}`, icon: 'trending_up' },
      { label: 'Utilisateurs', value: `${s.totalUsers}`, icon: 'people' },
      { label: 'Vérifiés', value: `${s.verifiedUsers}`, icon: 'verified_user' },
      { label: 'Suspendus', value: `${s.suspendedUsers}`, icon: 'person_off', accent: s.suspendedUsers > 0 ? 'warning' : undefined },
    ];
  }

  // --- Construction du graphique SVG (créées vs résolues par jour) ---

  get maxTrendValue(): number {
    const max = Math.max(1, ...this.trends().map((t) => Math.max(t.alertsCreated, t.alertsResolved)));
    return max;
  }

  barX(index: number): number {
    const usableWidth = this.chartWidth - this.chartPadding * 2;
    const step = usableWidth / Math.max(this.trends().length, 1);
    return this.chartPadding + index * step;
  }

  barWidth(): number {
    const usableWidth = this.chartWidth - this.chartPadding * 2;
    const step = usableWidth / Math.max(this.trends().length, 1);
    return Math.max(2, step * 0.35);
  }

  barHeight(value: number): number {
    const usableHeight = this.chartHeight - this.chartPadding * 2;
    return (value / this.maxTrendValue) * usableHeight;
  }

  barY(value: number): number {
    return this.chartHeight - this.chartPadding - this.barHeight(value);
  }

  formatShortDate(iso: string): string {
    const d = new Date(iso);
    return `${d.getDate()}/${d.getMonth() + 1}`;
  }

  shouldShowLabel(index: number): boolean {
    const everyNth = Math.ceil(this.trends().length / 8) || 1;
    return index % everyNth === 0;
  }
}

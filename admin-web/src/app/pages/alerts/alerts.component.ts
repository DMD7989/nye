import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { AlertService } from '../../core/services/alert.service';
import { AlertStatus, MissingAlert } from '../../core/models/alert.model';
import { ReasonDialogComponent } from '../../shared/dialogs/reason-dialog/reason-dialog.component';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatSelectModule,
    MatFormFieldModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './alerts.component.html',
  styleUrl: './alerts.component.scss',
})
export class AlertsComponent implements OnInit {
  readonly displayedColumns = ['photo', 'name', 'status', 'moderation', 'createdAt', 'actions'];
  readonly statusOptions: { value: AlertStatus | ''; label: string }[] = [
    { value: '', label: 'Tous les statuts' },
    { value: 'PENDING', label: 'En attente' },
    { value: 'ACTIVE', label: 'Active' },
    { value: 'RESOLVED', label: 'Résolue' },
    { value: 'REJECTED', label: 'Rejetée' },
  ];

  selectedStatus: AlertStatus | '' = 'PENDING';
  alerts = signal<MissingAlert[]>([]);
  loading = signal(true);

  constructor(
    private alertService: AlertService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.alertService.list(this.selectedStatus).subscribe({
      next: (alerts) => {
        this.alerts.set(alerts);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Impossible de charger les alertes.', 'Fermer', { duration: 4000 });
        this.loading.set(false);
      },
    });
  }

  validate(alert: MissingAlert): void {
    this.alertService.validate(alert.id).subscribe({
      next: () => {
        this.snackBar.open('Alerte validée et publiée.', 'OK', { duration: 3000 });
        this.load();
      },
      error: () => this.snackBar.open("Échec de la validation.", 'Fermer', { duration: 4000 }),
    });
  }

  reject(alert: MissingAlert): void {
    const dialogRef = this.dialog.open(ReasonDialogComponent, {
      width: '420px',
      data: {
        title: "Rejeter l'alerte",
        label: 'Motif du rejet (communiqué au déclarant)',
        confirmLabel: 'Rejeter',
      },
    });
    dialogRef.afterClosed().subscribe((reason) => {
      if (!reason) return;
      this.alertService.reject(alert.id, { reason }).subscribe({
        next: () => {
          this.snackBar.open('Alerte rejetée.', 'OK', { duration: 3000 });
          this.load();
        },
        error: () => this.snackBar.open('Échec du rejet.', 'Fermer', { duration: 4000 }),
      });
    });
  }

  close(alert: MissingAlert): void {
    const dialogRef = this.dialog.open(ReasonDialogComponent, {
      width: '420px',
      data: {
        title: "Clôturer l'alerte",
        label: 'Note de résolution',
        confirmLabel: 'Clôturer',
      },
    });
    dialogRef.afterClosed().subscribe((resolutionNote) => {
      if (!resolutionNote) return;
      this.alertService.close(alert.id, { resolutionNote }).subscribe({
        next: () => {
          this.snackBar.open('Alerte clôturée.', 'OK', { duration: 3000 });
          this.load();
        },
        error: () => this.snackBar.open('Échec de la clôture.', 'Fermer', { duration: 4000 }),
      });
    });
  }

  statusLabel(status: AlertStatus): string {
    return this.statusOptions.find((o) => o.value === status)?.label ?? status;
  }
}

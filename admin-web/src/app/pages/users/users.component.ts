import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { UserAdminService } from '../../core/services/user-admin.service';
import { AppUser } from '../../core/models/user.model';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss',
})
export class UsersComponent implements OnInit {
  readonly displayedColumns = ['name', 'phone', 'role', 'status', 'createdAt', 'actions'];
  users = signal<AppUser[]>([]);
  loading = signal(true);

  constructor(
    private userAdminService: UserAdminService,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.userAdminService.list().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Impossible de charger les utilisateurs.', 'Fermer', { duration: 4000 });
        this.loading.set(false);
      },
    });
  }

  toggleRole(user: AppUser): void {
    const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
    this.userAdminService.changeRole(user.id, newRole).subscribe({
      next: () => {
        this.snackBar.open(`Rôle mis à jour : ${newRole}.`, 'OK', { duration: 3000 });
        this.load();
      },
      error: () => this.snackBar.open('Échec du changement de rôle.', 'Fermer', { duration: 4000 }),
    });
  }

  toggleSuspension(user: AppUser): void {
    const action = user.enabled ? this.userAdminService.suspend(user.id) : this.userAdminService.reactivate(user.id);
    action.subscribe({
      next: () => {
        this.snackBar.open(user.enabled ? 'Compte suspendu.' : 'Compte réactivé.', 'OK', { duration: 3000 });
        this.load();
      },
      error: () => this.snackBar.open("Échec de l'opération.", 'Fermer', { duration: 4000 }),
    });
  }
}

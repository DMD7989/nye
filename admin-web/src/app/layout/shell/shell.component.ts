import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatButtonModule } from '@angular/material/button';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatSidenavModule, MatIconModule, MatButtonModule],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent {
  readonly navItems = [
    { path: '/dashboard', label: 'Tableau de bord', icon: 'dashboard' },
    { path: '/alerts', label: 'Alertes', icon: 'campaign' },
    { path: '/heatmap', label: 'Carte de chaleur', icon: 'map' },
    { path: '/users', label: 'Utilisateurs', icon: 'people' },
  ];

  constructor(
    public authService: AuthService,
    private router: Router,
  ) {}

  initials(): string {
    const name = this.authService.currentUser()?.fullName ?? '';
    const initials = name
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join('');
    return initials || 'A';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

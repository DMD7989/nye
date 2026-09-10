import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { API_BASE_URL } from '../api-config';
import { AppUser, AuthResponse, LoginRequest } from '../models/user.model';

const TOKEN_KEY = 'nye_admin_token';
const USER_KEY = 'nye_admin_user';

/**
 * Session admin : jeton JWT + identité de l'utilisateur connecté, persistés dans
 * localStorage pour survivre à un rechargement de page. L'accès à cette console est
 * réservé au rôle ADMIN (vérifié à la connexion et par le guard de route).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly currentUserSignal = signal<AppUser | null>(this.readStoredUser());

  readonly currentUser = computed(() => this.currentUserSignal());
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_BASE_URL}/api/auth/login`, request).pipe(
      tap((response) => {
        if (response.role !== 'ADMIN') {
          throw new Error("Ce compte n'a pas les droits administrateur.");
        }
        this.persistSession(response);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSignal.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  private persistSession(response: AuthResponse): void {
    const user: AppUser = {
      id: response.userId,
      fullName: response.fullName,
      phone: '',
      email: null,
      language: 'fr',
      role: response.role,
      enabled: true,
      phoneVerified: response.phoneVerified,
      notificationsEnabled: true,
      notificationRadiusKm: 10,
      createdAt: new Date().toISOString(),
    };
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.currentUserSignal.set(user);
  }

  private readStoredUser(): AppUser | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as AppUser;
    } catch {
      return null;
    }
  }
}

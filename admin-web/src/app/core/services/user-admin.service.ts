import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../api-config';
import { AppUser, Role } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserAdminService {
  constructor(private http: HttpClient) {}

  list(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${API_BASE_URL}/api/admin/users`);
  }

  changeRole(id: number, role: Role): Observable<AppUser> {
    const params = new HttpParams().set('role', role);
    return this.http.patch<AppUser>(`${API_BASE_URL}/api/admin/users/${id}/role`, {}, { params });
  }

  suspend(id: number): Observable<AppUser> {
    return this.http.patch<AppUser>(`${API_BASE_URL}/api/admin/users/${id}/suspend`, {});
  }

  reactivate(id: number): Observable<AppUser> {
    return this.http.patch<AppUser>(`${API_BASE_URL}/api/admin/users/${id}/reactivate`, {});
  }
}

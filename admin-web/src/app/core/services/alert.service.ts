import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../api-config';
import { AlertStatus, CloseAlertRequest, MissingAlert, RejectAlertRequest } from '../models/alert.model';

@Injectable({ providedIn: 'root' })
export class AlertService {
  constructor(private http: HttpClient) {}

  list(status?: AlertStatus | ''): Observable<MissingAlert[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<MissingAlert[]>(`${API_BASE_URL}/api/admin/alerts`, { params });
  }

  getById(id: number): Observable<MissingAlert> {
    return this.http.get<MissingAlert>(`${API_BASE_URL}/api/alerts/${id}`);
  }

  validate(id: number): Observable<MissingAlert> {
    return this.http.patch<MissingAlert>(`${API_BASE_URL}/api/admin/alerts/${id}/validate`, {});
  }

  close(id: number, request: CloseAlertRequest): Observable<MissingAlert> {
    return this.http.patch<MissingAlert>(`${API_BASE_URL}/api/admin/alerts/${id}/close`, request);
  }

  reject(id: number, request: RejectAlertRequest): Observable<MissingAlert> {
    return this.http.patch<MissingAlert>(`${API_BASE_URL}/api/admin/alerts/${id}/reject`, request);
  }
}

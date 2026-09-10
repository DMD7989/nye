import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../api-config';
import { AlertStatus } from '../models/alert.model';
import { DashboardSummary, HeatmapPoint, TrendPoint } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private http: HttpClient) {}

  getSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${API_BASE_URL}/api/admin/dashboard/summary`);
  }

  getTrends(days: number): Observable<TrendPoint[]> {
    const params = new HttpParams().set('days', days);
    return this.http.get<TrendPoint[]>(`${API_BASE_URL}/api/admin/dashboard/trends`, { params });
  }

  getHeatmap(status?: AlertStatus | ''): Observable<HeatmapPoint[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<HeatmapPoint[]>(`${API_BASE_URL}/api/admin/dashboard/heatmap`, { params });
  }
}

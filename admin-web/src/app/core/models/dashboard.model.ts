import { AlertStatus } from './alert.model';

export interface DashboardSummary {
  totalAlerts: number;
  pendingAlerts: number;
  activeAlerts: number;
  resolvedAlerts: number;
  rejectedAlerts: number;
  flaggedPendingAlerts: number;
  closureRatePercent: number | null;
  averageResolutionHours: number | null;
  newAlertsLast7Days: number;
  totalUsers: number;
  verifiedUsers: number;
  suspendedUsers: number;
}

export interface TrendPoint {
  date: string;
  alertsCreated: number;
  alertsResolved: number;
}

export interface HeatmapPoint {
  alertId: number;
  latitude: number;
  longitude: number;
  status: AlertStatus;
}

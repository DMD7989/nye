export type AlertStatus = 'PENDING' | 'ACTIVE' | 'RESOLVED' | 'REJECTED';

export interface MissingAlert {
  id: number;
  missingPersonName: string;
  missingPersonAge: number | null;
  description: string;
  photoUrl: string | null;
  photoPending: boolean;
  contactPhone: string;
  latitude: number;
  longitude: number;
  address: string | null;
  status: AlertStatus;
  createdById: number;
  createdByName: string | null;
  createdAt: string;
  validatedAt: string | null;
  resolvedAt: string | null;
  resolutionNote: string | null;
  moderationFlagged: boolean;
  moderationReason: string | null;
  rejectionReason: string | null;
  distanceKm: number | null;
  restricted: boolean;
}

export interface CloseAlertRequest {
  resolutionNote: string;
}

export interface RejectAlertRequest {
  reason: string;
}

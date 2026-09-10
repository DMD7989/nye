export type Role = 'USER' | 'ADMIN';

export interface AppUser {
  id: number;
  fullName: string;
  phone: string;
  email: string | null;
  language: string;
  role: Role;
  enabled: boolean;
  phoneVerified: boolean;
  notificationsEnabled: boolean;
  notificationRadiusKm: number;
  createdAt: string;
}

export interface LoginRequest {
  phone: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  fullName: string;
  role: Role;
  phoneVerified: boolean;
}

import 'package:dio/dio.dart';

import '../models/user.dart';
import 'api_client.dart';

class UserService {
  final ApiClient apiClient;

  UserService({required this.apiClient});

  Future<AppUser> updateProfile({
    required String fullName,
    String? email,
    required String language,
  }) async {
    final result = await _call(() => apiClient.dio.patch('/api/users/me', data: {
          'fullName': fullName,
          if (email != null) 'email': email,
          'language': language,
        }));
    return AppUser.fromJson(result.data as Map<String, dynamic>);
  }

  Future<void> updateLocation({required double latitude, required double longitude}) => _call(
      () => apiClient.dio.put('/api/users/me/location', data: {
            'latitude': latitude,
            'longitude': longitude,
          }));

  Future<void> updateFcmToken(String token) => _call(
      () => apiClient.dio.put('/api/users/me/fcm-token', data: {'token': token}));

  Future<AppUser> updateNotificationPreferences({required bool enabled, double? radiusKm}) async {
    final result = await _call(() => apiClient.dio.put('/api/users/me/notification-preferences', data: {
          'enabled': enabled,
          if (radiusKm != null) 'radiusKm': radiusKm,
        }));
    return AppUser.fromJson(result.data as Map<String, dynamic>);
  }

  Future<Response> _call(Future<Response> Function() request) async {
    try {
      return await request();
    } on DioException catch (e) {
      throw e.error is ApiException ? e.error as ApiException : const ApiException(null, 'Erreur inconnue');
    }
  }
}

import 'package:dio/dio.dart';

import '../models/user.dart';
import 'api_client.dart';
import 'token_storage.dart';

class AuthService {
  final ApiClient apiClient;
  final TokenStorage tokenStorage;

  AuthService({required this.apiClient, required this.tokenStorage});

  Future<AuthResult> register({
    required String fullName,
    required String phone,
    String? email,
    required String password,
  }) async {
    final result = await _call(() => apiClient.dio.post('/api/auth/register', data: {
          'fullName': fullName,
          'phone': phone,
          if (email != null && email.isNotEmpty) 'email': email,
          'password': password,
        }));
    final auth = AuthResult.fromJson(result.data as Map<String, dynamic>);
    await tokenStorage.save(auth.token);
    return auth;
  }

  Future<AuthResult> login({required String phone, required String password}) async {
    final result = await _call(() => apiClient.dio.post('/api/auth/login', data: {
          'phone': phone,
          'password': password,
        }));
    final auth = AuthResult.fromJson(result.data as Map<String, dynamic>);
    await tokenStorage.save(auth.token);
    return auth;
  }

  Future<void> requestOtp(String phone) => _call(
      () => apiClient.dio.post('/api/auth/otp/request', data: {'phone': phone}));

  Future<void> verifyOtp({required String phone, required String code}) => _call(
      () => apiClient.dio.post('/api/auth/otp/verify', data: {'phone': phone, 'code': code}));

  Future<AppUser> fetchProfile() async {
    final result = await _call(() => apiClient.dio.get('/api/users/me'));
    return AppUser.fromJson(result.data as Map<String, dynamic>);
  }

  Future<void> logout() => tokenStorage.clear();

  Future<bool> hasStoredToken() async => (await tokenStorage.read()) != null;

  Future<Response> _call(Future<Response> Function() request) async {
    try {
      return await request();
    } on DioException catch (e) {
      throw e.error is ApiException ? e.error as ApiException : const ApiException(null, 'Erreur inconnue');
    }
  }
}

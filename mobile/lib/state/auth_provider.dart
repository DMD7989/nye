import 'package:flutter/foundation.dart';

import '../models/user.dart';
import '../services/auth_service.dart';

enum AuthStatus { unknown, authenticated, unauthenticated }

class AuthProvider extends ChangeNotifier {
  final AuthService authService;

  AuthProvider({required this.authService});

  AuthStatus status = AuthStatus.unknown;
  AppUser? currentUser;

  Future<void> bootstrap() async {
    final hasToken = await authService.hasStoredToken();
    if (!hasToken) {
      status = AuthStatus.unauthenticated;
      notifyListeners();
      return;
    }
    try {
      currentUser = await authService.fetchProfile();
      status = AuthStatus.authenticated;
    } catch (_) {
      await authService.logout();
      status = AuthStatus.unauthenticated;
    }
    notifyListeners();
  }

  Future<void> login({required String phone, required String password}) async {
    await authService.login(phone: phone, password: password);
    currentUser = await authService.fetchProfile();
    status = AuthStatus.authenticated;
    notifyListeners();
  }

  Future<void> register({
    required String fullName,
    required String phone,
    String? email,
    required String password,
  }) async {
    await authService.register(fullName: fullName, phone: phone, email: email, password: password);
    currentUser = await authService.fetchProfile();
    status = AuthStatus.authenticated;
    notifyListeners();
  }

  Future<void> refreshProfile() async {
    currentUser = await authService.fetchProfile();
    notifyListeners();
  }

  Future<void> logout() async {
    await authService.logout();
    currentUser = null;
    status = AuthStatus.unauthenticated;
    notifyListeners();
  }
}

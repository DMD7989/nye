import 'package:flutter_test/flutter_test.dart';
import 'package:nye_mobile/models/user.dart';

void main() {
  group('AppUser.fromJson', () {
    test('parses a full response and exposes isAdmin', () {
      final user = AppUser.fromJson({
        'id': 1,
        'fullName': 'Admin N\'yé',
        'phone': '+22300000000',
        'email': 'admin@nye.ml',
        'language': 'fr',
        'role': 'ADMIN',
        'enabled': true,
        'phoneVerified': true,
        'notificationsEnabled': true,
        'notificationRadiusKm': 15.0,
        'createdAt': '2026-01-01T00:00:00Z',
      });

      expect(user.fullName, "Admin N'yé");
      expect(user.isAdmin, isTrue);
      expect(user.notificationRadiusKm, 15.0);
    });

    test('applies documented defaults when optional fields are absent', () {
      final user = AppUser.fromJson({
        'id': 2,
        'fullName': 'Utilisateur Démo',
        'phone': '+22311111111',
        'role': 'USER',
        'createdAt': '2026-01-01T00:00:00Z',
      });

      expect(user.email, isNull);
      expect(user.language, 'fr');
      expect(user.enabled, isTrue);
      expect(user.phoneVerified, isFalse);
      expect(user.notificationsEnabled, isTrue);
      expect(user.notificationRadiusKm, 10.0);
      expect(user.isAdmin, isFalse);
    });
  });

  group('AuthResult.fromJson', () {
    test('parses a login response', () {
      final result = AuthResult.fromJson({
        'token': 'jwt-token',
        'userId': 3,
        'fullName': 'Djimé Dembélé',
        'role': 'USER',
        'phoneVerified': true,
      });

      expect(result.token, 'jwt-token');
      expect(result.userId, 3);
      expect(result.phoneVerified, isTrue);
    });

    test('defaults phoneVerified to false when absent', () {
      final result = AuthResult.fromJson({
        'token': 'jwt-token',
        'userId': 3,
        'fullName': 'Djimé Dembélé',
        'role': 'USER',
      });

      expect(result.phoneVerified, isFalse);
    });
  });
}

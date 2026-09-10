import 'package:shared_preferences/shared_preferences.dart';

/// Stockage du jeton JWT sur l'appareil.
///
/// Utilise `shared_preferences` (non chiffré) plutôt qu'un stockage sécurisé
/// (Keystore/Keychain) : `flutter_secure_storage` récent entre en conflit de
/// dépendances transitives avec `geolocator` sur ce projet (win32 via
/// package_info_plus). À reconsidérer avant la mise en production — même
/// logique que les autres stubs "dev" du projet (OTP, FCM, stockage photo).
class TokenStorage {
  static const _tokenKey = 'nye_jwt_token';

  Future<void> save(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_tokenKey, token);
  }

  Future<String?> read() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_tokenKey);
  }

  Future<void> clear() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_tokenKey);
  }
}

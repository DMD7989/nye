import 'dart:io' show Platform;

import 'package:flutter/foundation.dart' show kIsWeb;

/// URL de base de l'API backend. En dev, l'émulateur Android ne peut pas
/// atteindre le "localhost" de la machine hôte directement : il faut passer
/// par l'alias spécial 10.0.2.2. Le web et le mode debug desktop utilisent
/// directement localhost.
class ApiConfig {
  static const String _override = String.fromEnvironment('NYE_API_BASE_URL');

  static String get baseUrl {
    if (_override.isNotEmpty) return _override;
    if (kIsWeb) return 'http://localhost:8080';
    if (Platform.isAndroid) return 'http://10.0.2.2:8080';
    return 'http://localhost:8080';
  }
}

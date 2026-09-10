class AppUser {
  final int id;
  final String fullName;
  final String phone;
  final String? email;
  final String language;
  final String role;
  final bool enabled;
  final bool phoneVerified;
  final bool notificationsEnabled;
  final double notificationRadiusKm;
  final DateTime createdAt;

  const AppUser({
    required this.id,
    required this.fullName,
    required this.phone,
    required this.language,
    required this.role,
    required this.enabled,
    required this.phoneVerified,
    required this.notificationsEnabled,
    required this.notificationRadiusKm,
    required this.createdAt,
    this.email,
  });

  bool get isAdmin => role == 'ADMIN';

  factory AppUser.fromJson(Map<String, dynamic> json) {
    return AppUser(
      id: json['id'] as int,
      fullName: json['fullName'] as String,
      phone: json['phone'] as String,
      email: json['email'] as String?,
      language: json['language'] as String? ?? 'fr',
      role: json['role'] as String,
      enabled: json['enabled'] as bool? ?? true,
      phoneVerified: json['phoneVerified'] as bool? ?? false,
      notificationsEnabled: json['notificationsEnabled'] as bool? ?? true,
      notificationRadiusKm:
          (json['notificationRadiusKm'] as num?)?.toDouble() ?? 10.0,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }
}

class AuthResult {
  final String token;
  final int userId;
  final String fullName;
  final String role;
  final bool phoneVerified;

  const AuthResult({
    required this.token,
    required this.userId,
    required this.fullName,
    required this.role,
    required this.phoneVerified,
  });

  factory AuthResult.fromJson(Map<String, dynamic> json) {
    return AuthResult(
      token: json['token'] as String,
      userId: json['userId'] as int,
      fullName: json['fullName'] as String,
      role: json['role'] as String,
      phoneVerified: json['phoneVerified'] as bool? ?? false,
    );
  }
}

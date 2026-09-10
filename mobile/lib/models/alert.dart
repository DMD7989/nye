enum AlertStatus { pending, active, resolved, rejected }

AlertStatus alertStatusFromString(String value) {
  switch (value) {
    case 'ACTIVE':
      return AlertStatus.active;
    case 'RESOLVED':
      return AlertStatus.resolved;
    case 'REJECTED':
      return AlertStatus.rejected;
    case 'PENDING':
    default:
      return AlertStatus.pending;
  }
}

class MissingAlert {
  final int id;
  final String missingPersonName;
  final int? missingPersonAge;
  final String description;
  final String? photoUrl;
  final bool photoPending;
  final String contactPhone;
  final double latitude;
  final double longitude;
  final String? address;
  final AlertStatus status;
  final int createdById;
  final String? createdByName;
  final DateTime createdAt;
  final DateTime? validatedAt;
  final DateTime? resolvedAt;
  final String? resolutionNote;
  final bool moderationFlagged;
  final String? moderationReason;
  final String? rejectionReason;
  final double? distanceKm;
  final bool restricted;

  const MissingAlert({
    required this.id,
    required this.missingPersonName,
    required this.description,
    required this.photoPending,
    required this.contactPhone,
    required this.latitude,
    required this.longitude,
    required this.status,
    required this.createdById,
    required this.createdAt,
    required this.moderationFlagged,
    required this.restricted,
    this.missingPersonAge,
    this.photoUrl,
    this.address,
    this.createdByName,
    this.validatedAt,
    this.resolvedAt,
    this.resolutionNote,
    this.moderationReason,
    this.rejectionReason,
    this.distanceKm,
  });

  factory MissingAlert.fromJson(Map<String, dynamic> json) {
    return MissingAlert(
      id: json['id'] as int,
      missingPersonName: json['missingPersonName'] as String,
      missingPersonAge: json['missingPersonAge'] as int?,
      description: json['description'] as String,
      photoUrl: json['photoUrl'] as String?,
      photoPending: json['photoPending'] as bool? ?? false,
      contactPhone: json['contactPhone'] as String,
      latitude: (json['latitude'] as num).toDouble(),
      longitude: (json['longitude'] as num).toDouble(),
      address: json['address'] as String?,
      status: alertStatusFromString(json['status'] as String),
      createdById: json['createdById'] as int,
      createdByName: json['createdByName'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
      validatedAt: json['validatedAt'] != null
          ? DateTime.parse(json['validatedAt'] as String)
          : null,
      resolvedAt: json['resolvedAt'] != null
          ? DateTime.parse(json['resolvedAt'] as String)
          : null,
      resolutionNote: json['resolutionNote'] as String?,
      moderationFlagged: json['moderationFlagged'] as bool? ?? false,
      moderationReason: json['moderationReason'] as String?,
      rejectionReason: json['rejectionReason'] as String?,
      distanceKm: (json['distanceKm'] as num?)?.toDouble(),
      restricted: json['restricted'] as bool? ?? false,
    );
  }
}

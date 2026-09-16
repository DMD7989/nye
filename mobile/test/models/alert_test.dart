import 'package:flutter_test/flutter_test.dart';
import 'package:nye_mobile/models/alert.dart';

void main() {
  group('alertStatusFromString', () {
    test('maps each known backend value', () {
      expect(alertStatusFromString('PENDING'), AlertStatus.pending);
      expect(alertStatusFromString('ACTIVE'), AlertStatus.active);
      expect(alertStatusFromString('RESOLVED'), AlertStatus.resolved);
      expect(alertStatusFromString('REJECTED'), AlertStatus.rejected);
    });

    test('falls back to pending on an unknown value', () {
      expect(alertStatusFromString('SOMETHING_NEW'), AlertStatus.pending);
    });
  });

  group('MissingAlert.fromJson', () {
    Map<String, dynamic> baseJson() => {
      'id': 1,
      'missingPersonName': 'Awa Traoré',
      'description': 'Vue pour la dernière fois au marché.',
      'photoPending': false,
      'contactPhone': '+22370000000',
      'latitude': 12.6392,
      'longitude': -8.0029,
      'status': 'ACTIVE',
      'createdById': 42,
      'createdAt': '2026-09-01T10:00:00Z',
      'moderationFlagged': false,
      'restricted': false,
    };

    test('parses a full response', () {
      final json = {
        ...baseJson(),
        'missingPersonAge': 34,
        'photoUrl': 'https://example.com/photo.jpg',
        'address': 'Marché de Médine, Bamako',
        'createdByName': 'Ibrahim Koné',
        'validatedAt': '2026-09-01T11:00:00Z',
        'resolvedAt': '2026-09-02T09:00:00Z',
        'resolutionNote': 'Retrouvée saine et sauve.',
        'moderationReason': null,
        'rejectionReason': null,
        'distanceKm': 3.5,
      };

      final alert = MissingAlert.fromJson(json);

      expect(alert.id, 1);
      expect(alert.missingPersonName, 'Awa Traoré');
      expect(alert.missingPersonAge, 34);
      expect(alert.status, AlertStatus.active);
      expect(alert.latitude, 12.6392);
      expect(alert.longitude, -8.0029);
      expect(alert.createdAt, DateTime.parse('2026-09-01T10:00:00Z'));
      expect(alert.validatedAt, DateTime.parse('2026-09-01T11:00:00Z'));
      expect(alert.distanceKm, 3.5);
    });

    test('defaults optional fields absent from a restricted response', () {
      // A PENDING alert seen by the public: the backend omits photoUrl,
      // address, moderation/resolution fields entirely rather than sending
      // null — the model must not throw on a missing key.
      final alert = MissingAlert.fromJson(baseJson());

      expect(alert.photoUrl, isNull);
      expect(alert.address, isNull);
      expect(alert.createdByName, isNull);
      expect(alert.validatedAt, isNull);
      expect(alert.resolvedAt, isNull);
      expect(alert.moderationFlagged, isFalse);
      expect(alert.distanceKm, isNull);
    });
  });
}

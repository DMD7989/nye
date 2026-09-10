import 'package:geolocator/geolocator.dart';

class LocationUnavailableException implements Exception {
  final String message;
  const LocationUnavailableException(this.message);

  @override
  String toString() => message;
}

class LocationService {
  Future<Position> getCurrentPosition() async {
    final serviceEnabled = await Geolocator.isLocationServiceEnabled();
    if (!serviceEnabled) {
      throw const LocationUnavailableException(
          "La localisation est désactivée sur cet appareil. Activez-la pour continuer.");
    }

    LocationPermission permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.denied) {
        throw const LocationUnavailableException(
            "La permission de localisation est requise pour signaler ou consulter une alerte.");
      }
    }
    if (permission == LocationPermission.deniedForever) {
      throw const LocationUnavailableException(
          "La permission de localisation est bloquée. Autorisez-la dans les paramètres de l'application.");
    }

    return Geolocator.getCurrentPosition(
      locationSettings: const LocationSettings(accuracy: LocationAccuracy.high),
    );
  }
}

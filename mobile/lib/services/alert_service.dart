import 'dart:io';

import 'package:dio/dio.dart';

import '../models/alert.dart';
import 'api_client.dart';

class AlertService {
  final ApiClient apiClient;

  AlertService({required this.apiClient});

  Future<List<MissingAlert>> listPublic({double? lat, double? lon, double? radiusKm}) async {
    final result = await _call(() => apiClient.dio.get('/api/alerts', queryParameters: {
          if (lat != null) 'lat': lat,
          if (lon != null) 'lon': lon,
          if (radiusKm != null) 'radiusKm': radiusKm,
        }));
    return (result.data as List)
        .map((e) => MissingAlert.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<List<MissingAlert>> listResolved() async {
    final result = await _call(() => apiClient.dio.get('/api/alerts/resolved'));
    return (result.data as List)
        .map((e) => MissingAlert.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<MissingAlert> getById(int id) async {
    final result = await _call(() => apiClient.dio.get('/api/alerts/$id'));
    return MissingAlert.fromJson(result.data as Map<String, dynamic>);
  }

  Future<MissingAlert> create({
    required String missingPersonName,
    int? missingPersonAge,
    required bool missingPersonIsMinor,
    required String description,
    required String photoUrl,
    required String contactPhone,
    required double latitude,
    required double longitude,
    String? address,
  }) async {
    final result = await _call(() => apiClient.dio.post('/api/alerts', data: {
          'missingPersonName': missingPersonName,
          if (missingPersonAge != null) 'missingPersonAge': missingPersonAge,
          'missingPersonIsMinor': missingPersonIsMinor,
          'description': description,
          'photoUrl': photoUrl,
          'contactPhone': contactPhone,
          'latitude': latitude,
          'longitude': longitude,
          if (address != null && address.isNotEmpty) 'address': address,
        }));
    return MissingAlert.fromJson(result.data as Map<String, dynamic>);
  }

  Future<String> uploadPhoto(File file) async {
    final formData = FormData.fromMap({
      'file': await MultipartFile.fromFile(file.path),
    });
    final result = await _call(() => apiClient.dio.post('/api/uploads/photo', data: formData));
    return (result.data as Map<String, dynamic>)['url'] as String;
  }

  Future<Response> _call(Future<Response> Function() request) async {
    try {
      return await request();
    } on DioException catch (e) {
      throw e.error is ApiException ? e.error as ApiException : const ApiException(null, 'Erreur inconnue');
    }
  }
}

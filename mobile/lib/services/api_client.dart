import 'package:dio/dio.dart';

import '../config/api_config.dart';
import 'token_storage.dart';

/// Exception applicative portant le message d'erreur renvoyé par le backend
/// (voir ApiError côté serveur), pour l'afficher tel quel à l'utilisateur.
class ApiException implements Exception {
  final int? statusCode;
  final String message;

  const ApiException(this.statusCode, this.message);

  @override
  String toString() => message;
}

class ApiClient {
  final TokenStorage tokenStorage;
  late final Dio dio;

  ApiClient({required this.tokenStorage}) {
    dio = Dio(BaseOptions(
      baseUrl: ApiConfig.baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 15),
    ));

    dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await tokenStorage.read();
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        handler.next(options);
      },
      onError: (error, handler) {
        handler.next(_wrap(error));
      },
    ));
  }

  DioException _wrap(DioException error) {
    final data = error.response?.data;
    String message = "Impossible de contacter le serveur. Vérifiez votre connexion.";
    if (data is Map && data['message'] != null) {
      message = data['message'].toString();
    } else if (error.response != null) {
      message = "Erreur du serveur (${error.response?.statusCode}).";
    }
    return error.copyWith(
      error: ApiException(error.response?.statusCode, message),
    );
  }
}

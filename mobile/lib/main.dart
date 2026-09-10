import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:provider/provider.dart';

import 'screens/splash_screen.dart';
import 'services/alert_service.dart';
import 'services/api_client.dart';
import 'services/auth_service.dart';
import 'services/location_service.dart';
import 'services/token_storage.dart';
import 'services/user_service.dart';
import 'state/auth_provider.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await initializeDateFormatting('fr_FR');

  final tokenStorage = TokenStorage();
  final apiClient = ApiClient(tokenStorage: tokenStorage);
  final authService = AuthService(apiClient: apiClient, tokenStorage: tokenStorage);
  final alertService = AlertService(apiClient: apiClient);
  final userService = UserService(apiClient: apiClient);
  final locationService = LocationService();

  runApp(NyeApp(
    authService: authService,
    alertService: alertService,
    userService: userService,
    locationService: locationService,
  ));
}

class NyeApp extends StatelessWidget {
  final AuthService authService;
  final AlertService alertService;
  final UserService userService;
  final LocationService locationService;

  const NyeApp({
    super.key,
    required this.authService,
    required this.alertService,
    required this.userService,
    required this.locationService,
  });

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider(authService: authService)..bootstrap()),
        Provider.value(value: alertService),
        Provider.value(value: userService),
        Provider.value(value: locationService),
      ],
      child: MaterialApp(
        title: "N'yé",
        debugShowCheckedModeBanner: false,
        locale: const Locale('fr', 'FR'),
        supportedLocales: const [Locale('fr', 'FR')],
        localizationsDelegates: const [
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFFB3541E)),
          useMaterial3: true,
          appBarTheme: const AppBarTheme(centerTitle: true),
        ),
        home: const SplashScreen(),
      ),
    );
  }
}

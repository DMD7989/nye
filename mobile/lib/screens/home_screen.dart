import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart' as ll;
import 'package:provider/provider.dart';

import '../models/alert.dart';
import '../services/alert_service.dart';
import '../services/location_service.dart';
import '../state/auth_provider.dart';
import '../widgets/alert_card.dart';
import 'alert_detail_screen.dart';
import 'create_alert_screen.dart';
import 'otp_screen.dart';
import 'profile_screen.dart';
import 'resolved_alerts_screen.dart';

/// Bamako, faute de position GPS disponible.
const _defaultCenter = ll.LatLng(12.6392, -8.0029);

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _tabIndex = 0;
  List<MissingAlert> _alerts = [];
  bool _loading = true;
  String? _error;
  ll.LatLng? _myPosition;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });

    double? lat;
    double? lon;
    try {
      final position = await context.read<LocationService>().getCurrentPosition();
      lat = position.latitude;
      lon = position.longitude;
      _myPosition = ll.LatLng(lat, lon);
    } catch (_) {
      // La géolocalisation est optionnelle pour consulter la carte : on continue sans tri par distance.
    }

    try {
      final alerts = await context.read<AlertService>().listPublic(lat: lat, lon: lon);
      setState(() => _alerts = alerts);
    } on Exception catch (e) {
      setState(() => _error = e.toString());
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  void _openCreateAlert() {
    final user = context.read<AuthProvider>().currentUser;
    if (user != null && !user.phoneVerified) {
      showDialog(
        context: context,
        builder: (_) => AlertDialog(
          title: const Text('Numéro non vérifié'),
          content: const Text(
              "Vous devez vérifier votre numéro de téléphone par code SMS avant de publier une alerte."),
          actions: [
            TextButton(onPressed: () => Navigator.pop(context), child: const Text('Annuler')),
            FilledButton(
              onPressed: () {
                Navigator.pop(context);
                Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => OtpScreen(phone: user.phone)),
                );
              },
              child: const Text('Vérifier maintenant'),
            ),
          ],
        ),
      );
      return;
    }

    Navigator.of(context)
        .push<bool>(MaterialPageRoute(builder: (_) => const CreateAlertScreen()))
        .then((created) {
      if (created == true) _load();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("N'yé"),
        actions: [
          IconButton(
            icon: const Icon(Icons.history),
            tooltip: 'Alertes résolues',
            onPressed: () => Navigator.of(context)
                .push(MaterialPageRoute(builder: (_) => const ResolvedAlertsScreen())),
          ),
          IconButton(
            icon: const Icon(Icons.person_outline),
            tooltip: 'Profil',
            onPressed: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => const ProfileScreen())),
          ),
        ],
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? _ErrorState(message: _error!, onRetry: _load)
              : RefreshIndicator(
                  onRefresh: _load,
                  child: _tabIndex == 0 ? _buildMap() : _buildList(),
                ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _openCreateAlert,
        icon: const Icon(Icons.campaign),
        label: const Text('Signaler'),
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _tabIndex,
        onDestinationSelected: (i) => setState(() => _tabIndex = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.map_outlined), selectedIcon: Icon(Icons.map), label: 'Carte'),
          NavigationDestination(icon: Icon(Icons.list_alt_outlined), selectedIcon: Icon(Icons.list_alt), label: 'Alertes'),
        ],
      ),
    );
  }

  Widget _buildMap() {
    return FlutterMap(
      options: MapOptions(
        initialCenter: _myPosition ?? _defaultCenter,
        initialZoom: 12,
      ),
      children: [
        TileLayer(
          urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
          userAgentPackageName: 'com.nye.mobile',
        ),
        MarkerLayer(
          markers: [
            if (_myPosition != null)
              Marker(
                point: _myPosition!,
                width: 40,
                height: 40,
                child: const Icon(Icons.my_location, color: Colors.blue),
              ),
            for (final alert in _alerts)
              Marker(
                point: ll.LatLng(alert.latitude, alert.longitude),
                width: 44,
                height: 44,
                child: GestureDetector(
                  onTap: () => _openDetail(alert),
                  child: Icon(
                    Icons.location_on,
                    size: 40,
                    color: alert.restricted ? Colors.orange : Colors.red,
                  ),
                ),
              ),
          ],
        ),
      ],
    );
  }

  Widget _buildList() {
    if (_alerts.isEmpty) {
      return ListView(
        children: const [
          SizedBox(height: 80),
          Center(child: Text('Aucune alerte active pour le moment.', style: TextStyle(color: Colors.black54))),
        ],
      );
    }
    return ListView.builder(
      padding: const EdgeInsets.symmetric(vertical: 8),
      itemCount: _alerts.length,
      itemBuilder: (context, index) {
        final alert = _alerts[index];
        return AlertCard(alert: alert, onTap: () => _openDetail(alert));
      },
    );
  }

  void _openDetail(MissingAlert alert) {
    Navigator.of(context).push(MaterialPageRoute(builder: (_) => AlertDetailScreen(alertId: alert.id)));
  }
}

class _ErrorState extends StatelessWidget {
  final String message;
  final VoidCallback onRetry;

  const _ErrorState({required this.message, required this.onRetry});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.error_outline, size: 48, color: Colors.grey),
            const SizedBox(height: 12),
            Text(message, textAlign: TextAlign.center),
            const SizedBox(height: 16),
            FilledButton(onPressed: onRetry, child: const Text('Réessayer')),
          ],
        ),
      ),
    );
  }
}

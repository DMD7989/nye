import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:intl/intl.dart';
import 'package:latlong2/latlong.dart' as ll;
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';

import '../models/alert.dart';
import '../services/alert_service.dart';
import '../services/api_client.dart';

class AlertDetailScreen extends StatefulWidget {
  final int alertId;

  const AlertDetailScreen({super.key, required this.alertId});

  @override
  State<AlertDetailScreen> createState() => _AlertDetailScreenState();
}

class _AlertDetailScreenState extends State<AlertDetailScreen> {
  MissingAlert? _alert;
  bool _loading = true;
  String? _error;

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
    try {
      final alert = await context.read<AlertService>().getById(widget.alertId);
      setState(() => _alert = alert);
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _call(String phone) async {
    final uri = Uri(scheme: 'tel', path: phone);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Détail de l'alerte")),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(child: Text(_error!))
              : _buildContent(_alert!),
    );
  }

  Widget _buildContent(MissingAlert alert) {
    final dateFormat = DateFormat('d MMMM yyyy, HH:mm', 'fr_FR');

    return SingleChildScrollView(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (alert.photoPending || alert.photoUrl == null)
            Container(
              height: 240,
              color: Colors.grey.shade200,
              child: const Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.hourglass_top, size: 40, color: Colors.grey),
                    SizedBox(height: 8),
                    Text('Photo masquée en attente de validation', style: TextStyle(color: Colors.grey)),
                  ],
                ),
              ),
            )
          else
            Image.network(alert.photoUrl!, height: 240, width: double.infinity, fit: BoxFit.cover),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Text(alert.missingPersonName,
                          style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold)),
                    ),
                    _StatusChip(status: alert.status),
                  ],
                ),
                if (alert.missingPersonAge != null)
                  Padding(
                    padding: const EdgeInsets.only(top: 4),
                    child: Text('${alert.missingPersonAge} ans', style: const TextStyle(color: Colors.black54)),
                  ),
                if (alert.restricted)
                  Container(
                    margin: const EdgeInsets.only(top: 12),
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: Colors.orange.shade50,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: const Row(
                      children: [
                        Icon(Icons.info_outline, size: 18, color: Colors.orange),
                        SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            "En attente de validation par un administrateur : la position affichée est approximative.",
                            style: TextStyle(fontSize: 13),
                          ),
                        ),
                      ],
                    ),
                  ),
                if (alert.status == AlertStatus.rejected && alert.rejectionReason != null)
                  Container(
                    margin: const EdgeInsets.only(top: 12),
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: Colors.red.shade50,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text('Alerte rejetée : ${alert.rejectionReason}',
                        style: TextStyle(color: Colors.red.shade700)),
                  ),
                if (alert.status == AlertStatus.resolved && alert.resolutionNote != null)
                  Container(
                    margin: const EdgeInsets.only(top: 12),
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: Colors.green.shade50,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text('Résolue : ${alert.resolutionNote}', style: TextStyle(color: Colors.green.shade800)),
                  ),
                const Divider(height: 32),
                Text(alert.description, style: const TextStyle(fontSize: 15)),
                const SizedBox(height: 16),
                if (alert.address != null) ...[
                  Row(
                    children: [
                      const Icon(Icons.place_outlined, size: 18, color: Colors.grey),
                      const SizedBox(width: 6),
                      Expanded(child: Text(alert.address!)),
                    ],
                  ),
                  const SizedBox(height: 8),
                ],
                Row(
                  children: [
                    const Icon(Icons.access_time, size: 18, color: Colors.grey),
                    const SizedBox(width: 6),
                    Text('Signalée le ${dateFormat.format(alert.createdAt.toLocal())}'),
                  ],
                ),
                const SizedBox(height: 16),
                SizedBox(
                  height: 180,
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(12),
                    child: FlutterMap(
                      options: MapOptions(
                        initialCenter: ll.LatLng(alert.latitude, alert.longitude),
                        initialZoom: 14,
                        interactionOptions: const InteractionOptions(flags: InteractiveFlag.none),
                      ),
                      children: [
                        TileLayer(
                          urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                          userAgentPackageName: 'com.nye.mobile',
                        ),
                        MarkerLayer(markers: [
                          Marker(
                            point: ll.LatLng(alert.latitude, alert.longitude),
                            width: 40,
                            height: 40,
                            child: const Icon(Icons.location_on, size: 36, color: Colors.red),
                          ),
                        ]),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 24),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton.icon(
                    onPressed: () => _call(alert.contactPhone),
                    icon: const Icon(Icons.phone),
                    label: Text('Appeler ${alert.contactPhone}'),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _StatusChip extends StatelessWidget {
  final AlertStatus status;

  const _StatusChip({required this.status});

  @override
  Widget build(BuildContext context) {
    final (label, color) = switch (status) {
      AlertStatus.pending => ('En attente', Colors.orange),
      AlertStatus.active => ('Active', Colors.red),
      AlertStatus.resolved => ('Résolue', Colors.green),
      AlertStatus.rejected => ('Rejetée', Colors.grey),
    };
    return Chip(
      label: Text(label, style: const TextStyle(color: Colors.white, fontSize: 12)),
      backgroundColor: color,
      padding: EdgeInsets.zero,
      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
    );
  }
}

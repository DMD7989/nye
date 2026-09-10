import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/alert.dart';
import '../services/alert_service.dart';
import '../services/api_client.dart';
import '../widgets/alert_card.dart';
import 'alert_detail_screen.dart';

class ResolvedAlertsScreen extends StatefulWidget {
  const ResolvedAlertsScreen({super.key});

  @override
  State<ResolvedAlertsScreen> createState() => _ResolvedAlertsScreenState();
}

class _ResolvedAlertsScreenState extends State<ResolvedAlertsScreen> {
  List<MissingAlert> _alerts = [];
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
      final alerts = await context.read<AlertService>().listResolved();
      setState(() => _alerts = alerts);
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Alertes résolues')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(child: Text(_error!))
              : _alerts.isEmpty
                  ? const Center(child: Text('Aucune alerte résolue pour le moment.'))
                  : RefreshIndicator(
                      onRefresh: _load,
                      child: ListView.builder(
                        padding: const EdgeInsets.symmetric(vertical: 8),
                        itemCount: _alerts.length,
                        itemBuilder: (context, index) {
                          final alert = _alerts[index];
                          return AlertCard(
                            alert: alert,
                            onTap: () => Navigator.of(context)
                                .push(MaterialPageRoute(builder: (_) => AlertDetailScreen(alertId: alert.id))),
                          );
                        },
                      ),
                    ),
    );
  }
}

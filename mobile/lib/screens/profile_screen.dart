import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../services/api_client.dart';
import '../services/user_service.dart';
import '../state/auth_provider.dart';
import 'login_screen.dart';
import 'otp_screen.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  late final TextEditingController _nameController;
  late final TextEditingController _emailController;
  late bool _notificationsEnabled;
  late double _radiusKm;
  bool _saving = false;
  String? _message;
  bool _isError = false;

  @override
  void initState() {
    super.initState();
    final user = context.read<AuthProvider>().currentUser!;
    _nameController = TextEditingController(text: user.fullName);
    _emailController = TextEditingController(text: user.email ?? '');
    _notificationsEnabled = user.notificationsEnabled;
    _radiusKm = user.notificationRadiusKm;
  }

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    super.dispose();
  }

  Future<void> _saveProfile() async {
    setState(() {
      _saving = true;
      _message = null;
    });
    try {
      await context.read<UserService>().updateProfile(
            fullName: _nameController.text.trim(),
            email: _emailController.text.trim().isEmpty ? null : _emailController.text.trim(),
            language: 'fr',
          );
      await context.read<AuthProvider>().refreshProfile();
      setState(() {
        _message = 'Profil mis à jour.';
        _isError = false;
      });
    } on ApiException catch (e) {
      setState(() {
        _message = e.message;
        _isError = true;
      });
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  Future<void> _saveNotificationPreferences() async {
    setState(() {
      _saving = true;
      _message = null;
    });
    try {
      await context.read<UserService>().updateNotificationPreferences(
            enabled: _notificationsEnabled,
            radiusKm: _radiusKm,
          );
      await context.read<AuthProvider>().refreshProfile();
      setState(() {
        _message = 'Préférences de notifications mises à jour.';
        _isError = false;
      });
    } on ApiException catch (e) {
      setState(() {
        _message = e.message;
        _isError = true;
      });
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  Future<void> _logout() async {
    await context.read<AuthProvider>().logout();
    if (!mounted) return;
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginScreen()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    final user = context.watch<AuthProvider>().currentUser!;

    return Scaffold(
      appBar: AppBar(title: const Text('Mon profil')),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            if (_message != null)
              Container(
                margin: const EdgeInsets.only(bottom: 16),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: _isError ? Colors.red.shade50 : Colors.green.shade50,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(_message!,
                    style: TextStyle(color: _isError ? Colors.red.shade700 : Colors.green.shade800)),
              ),
            if (!user.phoneVerified)
              Card(
                color: Colors.orange.shade50,
                child: ListTile(
                  leading: const Icon(Icons.warning_amber_outlined, color: Colors.orange),
                  title: const Text('Numéro non vérifié'),
                  subtitle: const Text('Vérifiez votre téléphone pour pouvoir publier une alerte.'),
                  trailing: TextButton(
                    onPressed: () =>
                        Navigator.of(context).push(MaterialPageRoute(builder: (_) => OtpScreen(phone: user.phone))),
                    child: const Text('Vérifier'),
                  ),
                ),
              ),
            const SizedBox(height: 8),
            Text('Informations', style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 12),
            TextField(
              controller: _nameController,
              decoration: const InputDecoration(labelText: 'Nom complet', border: OutlineInputBorder()),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _emailController,
              keyboardType: TextInputType.emailAddress,
              decoration: const InputDecoration(labelText: 'Email', border: OutlineInputBorder()),
            ),
            const SizedBox(height: 8),
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 8),
              child: Text('Téléphone : ${user.phone}', style: const TextStyle(color: Colors.black54)),
            ),
            Align(
              alignment: Alignment.centerRight,
              child: FilledButton(
                onPressed: _saving ? null : _saveProfile,
                child: const Text('Enregistrer'),
              ),
            ),
            const Divider(height: 40),
            Text('Notifications', style: Theme.of(context).textTheme.titleMedium),
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              title: const Text('Alertes géolocalisées'),
              subtitle: const Text('Recevoir une notification quand une alerte est validée à proximité'),
              value: _notificationsEnabled,
              onChanged: (v) => setState(() => _notificationsEnabled = v),
            ),
            Text('Rayon : ${_radiusKm.toStringAsFixed(0)} km'),
            Slider(
              value: _radiusKm,
              min: 1,
              max: 50,
              divisions: 49,
              label: '${_radiusKm.toStringAsFixed(0)} km',
              onChanged: _notificationsEnabled ? (v) => setState(() => _radiusKm = v) : null,
            ),
            Align(
              alignment: Alignment.centerRight,
              child: FilledButton(
                onPressed: _saving ? null : _saveNotificationPreferences,
                child: const Text('Enregistrer'),
              ),
            ),
            const Divider(height: 40),
            OutlinedButton.icon(
              onPressed: _logout,
              icon: const Icon(Icons.logout),
              label: const Text('Se déconnecter'),
              style: OutlinedButton.styleFrom(foregroundColor: Colors.red),
            ),
          ],
        ),
      ),
    );
  }
}

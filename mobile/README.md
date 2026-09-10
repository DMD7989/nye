# N'yé — Application mobile

Application Flutter pour signaler une disparition, consulter les alertes à proximité et recevoir des
notifications géolocalisées.

## Stack

- Flutter 3.44 / Dart 3.12
- `dio` — client HTTP
- `flutter_secure_storage` — stockage du jeton JWT (Keystore Android / Keychain iOS)
- `flutter_map` + `latlong2` — carte interactive (tuiles OpenStreetMap, aucune clé API requise)
- `geolocator` — position GPS de l'appareil
- `image_picker` — photo (caméra ou galerie)
- `provider` — état applicatif (auth, injection des services)
- `url_launcher` — bouton d'appel téléphonique

## Lancer en local

Le backend doit tourner sur `http://localhost:8080` (voir `../backend/README.md`, profil `dev`).

```bash
# Émulateur Android : le backend est vu via l'alias spécial 10.0.2.2, pas localhost
flutter run -d emulator-5554 --dart-define=NYE_API_BASE_URL=http://10.0.2.2:8080

# Web (Chrome) : localhost fonctionne directement, pas besoin de --dart-define
flutter run -d chrome
```

`ApiConfig.baseUrl` (`lib/config/api_config.dart`) choisit automatiquement l'URL adaptée
(`10.0.2.2` sur Android, `localhost` ailleurs) ; `--dart-define=NYE_API_BASE_URL=...` permet de la
forcer (utile pour pointer vers un vrai serveur de dev déployé, par exemple).

Comptes de test déjà seedés côté backend (profil `dev`) :
- Admin — `+22300000000` / `Admin1234!`
- Utilisateur démo (déjà vérifié) — `+22311111111` / `Demo1234!`

## Structure

```
lib/
  config/       # URL de l'API
  models/       # AppUser, MissingAlert — miroir des DTO du backend
  services/     # ApiClient (Dio + intercepteur JWT), AuthService, AlertService,
                # UserService, LocationService, TokenStorage
  state/        # AuthProvider (ChangeNotifier) — session courante
  screens/      # Un écran par vue (voir ci-dessous)
  widgets/      # Composants réutilisés (AlertCard)
```

## Écrans et fonctionnalités couvertes

| Écran | Backlog | Description |
|---|---|---|
| `login_screen.dart` / `register_screen.dart` / `otp_screen.dart` | Nyé-F1 | Connexion, inscription, vérification OTP |
| `home_screen.dart` | Nyé-F3 | Carte interactive + liste des alertes en attente/actives, triées par distance si la position est disponible |
| `create_alert_screen.dart` | Nyé-F2 | Formulaire de signalement : photo (obligatoire), description, position GPS auto, contact |
| `alert_detail_screen.dart` | Nyé-F4 | Détail complet, mini-carte, bouton d'appel |
| `resolved_alerts_screen.dart` | Nyé-F6 | Historique des alertes clôturées |
| `profile_screen.dart` | Nyé-F7 | Profil, préférences de notifications (activer/désactiver, rayon en km) |

## Règles de visibilité côté client

Le backend renvoie `restricted: true` pour une alerte pas encore validée (ou rejetée) : position
floutée, photo masquée (`photoPending: true`), nom réduit pour un mineur (§13.1). L'app ne fait
aucun filtrage supplémentaire — elle affiche fidèlement ce que l'API renvoie déjà correctement
adapté au visiteur (ex. l'auteur voit toujours sa propre alerte en clair).

Créer une alerte est bloqué côté app (avec redirection vers l'écran OTP) tant que
`phoneVerified` est `false` sur le profil courant — le backend applique la même règle
indépendamment (403 sinon), donc le contrôle côté app n'est qu'un confort UX.

## Notifications push (Nyé-F5) — non branché dans cette itération

Le backend expose déjà `PUT /api/users/me/fcm-token` et sait filtrer les destinataires par rayon
(voir `backend/README.md`). Côté app, l'intégration `firebase_messaging` n'a pas été ajoutée dans
cette itération car elle nécessite un vrai projet Firebase configuré (`flutterfire configure`,
`google-services.json`) pour être testable — même limitation que le stub FCM du backend. Le point
d'intégration est clair : après connexion, appeler `UserService.updateFcmToken(...)` avec le jeton
obtenu via `FirebaseMessaging.instance.getToken()`.

## À faire ensuite

- Intégration FCM réelle (nécessite un projet Firebase)
- Appel périodique de `UserService.updateLocation(...)` en arrière-plan pour tenir la position à jour
  (actuellement mise à jour uniquement au moment de créer une alerte — pas de suivi continu, par
  choix de confidentialité, voir cahier des charges §13.1)
- Écran de gestion des alertes de l'utilisateur connecté ("mes alertes")
- Tests widgets/intégration (`integration_test`) contre un backend de test
- Internationalisation (l'app est actuellement 100% en français, en dur)

# N'yé

Plateforme numérique de signalement et de recherche de personnes disparues au Mali.

Dépôt GitHub : [github.com/DMD7989/nye](https://github.com/DMD7989/nye)

Voir [docs/Cahier de charges.docx](docs/Cahier%20de%20charges.docx) pour les exigences complètes.

## Structure du projet

- `backend/` — API REST Spring Boot (Java 21), JWT + OTP, H2 en dev / PostgreSQL en prod. Voir [backend/README.md](backend/README.md).
- `mobile/` — application Flutter : inscription/OTP, fil d'alertes, création/détail d'alerte, historique, profil. Voir [mobile/README.md](mobile/README.md).
- `admin-web/` — console d'administration Angular : connexion, tableau de bord, modération des alertes, gestion des utilisateurs, carte de chaleur. Voir [admin-web/README.md](admin-web/README.md).
- `docs/` — cahier des charges et documents de conception.

## État d'avancement

- [x] Backend : authentification JWT/OTP, cycle de vie complet des alertes (création, validation, rejet, clôture), visibilité restreinte avant validation, modération de contenu, notifications push géolocalisées (stub en dev), dashboard analytique, 43 tests
- [x] Application mobile Flutter (code complet — non encore validé sur appareil réel)
- [x] Plateforme admin Angular (connexion, dashboard, modération, utilisateurs, carte de chaleur)
- [ ] Intégration réelle : passerelle SMS, projet Firebase (FCM), service de modération d'image
- [ ] Politique de rétention/anonymisation des alertes clôturées
- [ ] Diagrammes UML / MCD formels

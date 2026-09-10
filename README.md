# N'yé

Plateforme numérique de signalement et de recherche de personnes disparues au Mali.

Voir [docs/Cahier de charges.docx](docs/Cahier%20de%20charges.docx) pour les exigences complètes.

## Structure du projet

- `backend/` — API REST Spring Boot (Java 21), JWT, H2 en dev / PostgreSQL en prod. Voir [backend/README.md](backend/README.md).
- `mobile/` — application Flutter (à venir) : publication d'alertes, carte interactive, notifications.
- `admin-web/` — plateforme d'administration Angular (à venir) : validation des alertes, dashboard, modération.
- `docs/` — cahier des charges et documents de conception.

## État d'avancement

- [x] Backend : authentification JWT, gestion des alertes (création, validation, clôture), gestion des utilisateurs/rôles, API documentée (Swagger)
- [ ] Application mobile Flutter
- [ ] Plateforme admin Angular
- [ ] Notifications push (FCM)
- [ ] Diagrammes UML / MCD formels

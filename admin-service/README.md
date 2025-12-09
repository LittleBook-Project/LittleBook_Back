# admin-service — Microservice Administration

Ce dossier contient le microservice **admin-service**, chargé de l'administration, la surveillance et les statistiques du système LittleBook.
Il expose des opérations permettant de **monitorer** les activités utilisateurs, **gérer** les données administratives et **collecter** les statistiques d'utilisation.

Ce README couvre uniquement le périmètre du microservice `admin-service`.

---

## 📋 Rôle du microservice

Le service fournit :

* Monitoring des statistiques utilisateurs (connexions, activités)
* Gestion des événements de connexion et logs
* Suivi des activités de révision (reviews)
* Métriques d'utilisation globales du système
* API REST pour les opérations administratives
* Communication inter-services avec user-service et review-service

---

## 🏗️ Structure du projet

```
src/main/java/com/littlebook/admin
 ├── AdminApplication.java              → Entrypoint Spring Boot
 ├── config/
 │   ├── OpenApiConfig.java            → Configuration Swagger/OpenAPI
 │   ├── RestTemplateConfig.java       → Configuration HTTP pour inter-services
 │   └── SecurityConfig.java           → Configuration de sécurité
 ├── controller/
 │   ├── AdminController.java          → Endpoints administratifs
 │   ├── HealthController.java         → Health checks
 │   └── StatsController.java          → Endpoints statistiques
 ├── service/
 │   ├── AdminService.java             → Logique métier administrative
 │   ├── StatsService.java             → Calcul des statistiques
 │   └── UserClient.java               → Client HTTP pour user-service
 ├── dto/
 │   ├── AdminUserDto.java             → DTO Utilisateur
 │   ├── LoginRecordRequest.java       → DTO pour enregistrer une connexion
 │   ├── SetLastLoginRequest.java      → DTO pour mise à jour connexion
 │   ├── SetTotalLoginsRequest.java    → DTO pour mise à jour compteur
 │   └── (autres DTOs métier)
 ├── entity/
 │   ├── LoginEvent.java               → Entité événements de connexion
 │   ├── ReviewActivity.java           → Entité activités de révision
 │   └── UserLoginStats.java           → Entité statistiques utilisateur
 ├── repository/
 │   ├── AdminRepository.java          → Repository administration
 │   ├── LoginEventRepository.java     → Repository événements
 │   ├── ReviewActivityRepository.java → Repository activités révision
 │   └── UserLoginStatsRepository.java → Repository stats
 └── exception/
     └── (Gestion des erreurs)

src/main/resources/application.yml      → Configuration
Dockerfile                               → Build Docker
```

---

## 🚀 Principales caractéristiques

* **Entrypoint** : `com.littlebook.admin.AdminApplication`
* **Port par défaut** : `8085`
* **API** : `/admin/*` et `/stats/*`
* **Base de données** : H2 en mémoire pour développement
* **Inter-services** : Communication avec user-service et review-service
* **Monitoring** : H2 Console disponible à `/h2-console`

---

## 🔗 Endpoints principaux

### Santé & Diagnostics
* `GET /admin/health` — health check du service
* `GET /health` — endpoint de base pour les health checks

### Gestion Utilisateurs Administratifs
* `POST /admin/users` — créer un utilisateur administratif
* `GET /admin/users/{id}` — récupérer un utilisateur
* `GET /admin/users` — lister tous les utilisateurs
* `PUT /admin/users/{id}` — mettre à jour un utilisateur
* `DELETE /admin/users/{id}` — supprimer un utilisateur

### Événements de Connexion
* `POST /admin/login-events` — enregistrer un événement de connexion
* `GET /admin/login-events` — récupérer l'historique des connexions
* `GET /admin/login-events/{userId}` — connexions d'un utilisateur spécifique

### Statistiques d'Utilisation
* `GET /stats/login-stats` — statistiques globales de connexion
* `GET /stats/user-login-stats/{userId}` — stats d'un utilisateur
* `GET /stats/review-activity` — activité des reviews
* `GET /stats/dashboard` — dashboard global

---

## 🛠️ Prérequis

* Java 17+
* Maven 3.8+
* Docker (optionnel)
* Accès réseau pour la communication inter-services

---

## 🔌 Communication Inter-Services

### User-Service Client
* **Classe** : `com.littlebook.admin.service.UserClient`
* **Endpoint base** : Configuré dans `application.yml`
* **Opérations** :
  - Récupération de profils utilisateurs
  - Synchronisation des données de connexion
  - Mise à jour des statistiques utilisateur
* **RestTemplate** : Configuré dans `RestTemplateConfig.java`

### Intégrations
- **user-service** : Récupération et mise à jour des données utilisateur
- **review-service** : Collecte des activités de révision
- **book-service** : Référence pour les statistiques (optionnel)

---

## 💻 Développement — démarrer localement

1. Build :

```powershell
cd admin-service
mvn -DskipTests clean package
```

2. Run via Maven :

```powershell
mvn spring-boot:run
```

3. Ou exécuter le jar :

```powershell
java -jar target/littlebook-back-0.0.1-SNAPSHOT.jar
```

Le service démarre sur `http://localhost:8085`

Tests rapides :

```powershell
curl.exe -X GET "http://localhost:8085/admin/health"
curl.exe -X GET "http://localhost:8085/admin/users"
curl.exe -X GET "http://localhost:8085/stats/dashboard"
```

Accès H2 Console :

```
http://localhost:8085/h2-console
URL: jdbc:h2:mem:admindb
Driver: org.h2.Driver
```

---

## 🐳 Docker

Build :

```powershell
docker build -t littlebook-admin:local admin-service/
```

Run via Docker Compose :

```powershell
docker-compose up -d admin-service
```

Le service écoute sur le port `8085`

---

## 🗄️ Base de données

* **Type** : H2 en mémoire pour le développement
* **Configuration** : `src/main/resources/application.yml`
* **URL** : `jdbc:h2:mem:admindb`
* **Console** : Disponible à `http://localhost:8085/h2-console`
* **DDL** : Hibernate `ddl-auto: update` (crée les tables automatiquement)

### Entités principales
- **LoginEvent** : Historique des connexions utilisateur
- **ReviewActivity** : Activités de révision/review
- **UserLoginStats** : Statistiques agrégées par utilisateur

---

## 🧪 Tests

Lancer les tests :

```bash
mvn test
```

Ajouter selon besoin :

* tests unitaires (services, mapping DTO)
* tests d’intégration (WebTestClient / MockMvc)
* tests sur l’intégration OpenLibrary (mock de l’API)

---

## 🔐 Sécurité

Configuration basique pour le développement. Avant production :

* Ajouter authentification (JWT / Firebase / OAuth2)
* Restreindre les endpoints aux administrateurs
* Valider les appels inter-services (HTTPS, mTLS)
* Audit trail complet des opérations sensibles

---

## 📄 Swagger / OpenAPI

Disponible via springdoc :

```
http://localhost:8085/swagger-ui.html
```

La documentation interactive affiche tous les endpoints avec les modèles de données.

---

## 📌 Fonctionnalités implémentées

- ✅ **CRUD Utilisateurs Admin** : Création, lecture, mise à jour, suppression
- ✅ **Enregistrement Événements** : Logging des connexions utilisateur
- ✅ **Statistiques Utilisateur** : Agrégation des connexions par utilisateur
- ✅ **Suivi Activités** : Enregistrement des activités de review
- ✅ **Dashboard Global** : Vue synthétique des statistiques d'utilisation
- ✅ **H2 en mémoire** : Démarrage rapide en développement
- ✅ **Inter-services** : Communication avec user-service via RestTemplate
- ✅ **Pagination** : Support sur tous les endpoints liste
- ✅ **Gestion d'erreurs** : Réponses standardisées
- ✅ **Health Checks** : Endpoints dédiés pour la surveillance

## 🔮 Perspectives futures

### Court terme (1-2 mois)
1. **JWT Authentication** — Sécuriser l'accès aux endpoints administratifs
2. **Audit Logging** — Tracer toutes les modifications administratives
3. **Tests unitaires** — Coverage > 80% avec JUnit5 + Mockito
4. **Real-time Alerts** — Notifications sur seuils critiques atteints

### Moyen terme (3-6 mois)
1. **PostgreSQL** — Migration vers PostgreSQL en production
2. **Elasticsearch** — Indexation des logs pour recherches rapides
3. **Grafana Dashboards** — Visualisation avancée des métriques
4. **Prometheus Metrics** — Exposition des métriques système
5. **WebSocket Notifications** — Alertes temps réel aux administrateurs

### Long terme (6-12 mois)
1. **Machine Learning** — Détection d'anomalies d'usage
2. **Report Generation** — Génération de rapports PDF/Excel
3. **Role-Based Access Control** — Niveaux de permission granulaires
4. **Audit Trail Immutable** — Tamper-proof logging
5. **Integration Analytics** — Intégration avec Google Analytics / Mixpanel

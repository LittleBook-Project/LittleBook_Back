# auth-service  Microservice Authentication

Ce dossier contient le microservice **auth-service**, chargé de l'authentification stateless pour LittleBook.
Il valide les **ID tokens Firebase** (Google / Microsoft) et expose des endpoints REST minimalistes pour vérifier la disponibilité et récupérer le profil courant.

Ce README couvre uniquement le périmètre du microservice `auth-service`.

---

##  Rôle du microservice

Le service fournit :

* Validation des ID tokens Firebase via `FirebaseAuth.verifyIdToken()`
* Endpoints REST : ping public et profil protégé
* Authentification stateless (aucune base de données)
* Scaling horizontal facile
* CORS configurable par propriétés
* API REST exposée au front-end et aux autres microservices
* Swagger UI uniquement en profil `dev`

Stack : Java 17, Spring Boot 3.3.x, Spring Security 6, Firebase Admin SDK, springdoc OpenAPI.

---

##  Structure du projet

```
src/main/java/com/littlebook/auth
  AuthApplication.java                 Entrypoint Spring Boot
  api/
     AuthController.java              Endpoints /api/public/ping, /api/auth/me
  config/
     FirebaseConfig.java              Init FirebaseApp / FirebaseAuth
     CorsConfig.java                  Bean CorsConfigurationSource
     CorsProperties.java              @ConfigurationProperties security.cors.*
     OpenApiConfig.java (@Profile dev)  Swagger/OpenAPI en dev uniquement
  security/
      FirebaseTokenFilter.java         Vérification Bearer Firebase ID token
      SecurityConfig.java              Spring Security stateless + CORS + filtres

src/main/resources/application.yml         Config (port 8081, CORS, firebase, springdoc)
Dockerfile                                  Build Docker
```

---

##  Principales caractéristiques

* **Entrypoint** : `com.littlebook.auth.AuthApplication`
* **Port par défaut** : `8081`
* **API** : `/api/public/*` et `/api/auth/*`
* **Base de données** : Aucune (stateless)
* **OAuth Providers** : Google, Microsoft (via Firebase)
* **Monitoring** : Swagger UI (profil `dev` uniquement)
* **Sécurité** : Spring Security 6 + FirebaseTokenFilter

---

##  Configuration (application.yml extrait)

```yaml
server:
  port: 8081

security:
  cors:
    allowed-origins:
      - "http://localhost:80"
      - "http://localhost:5173"
      - "http://127.0.0.1:5500"
      - "http://localhost:5500"
    allowed-methods: [GET, POST, PUT, PATCH, DELETE, OPTIONS]
    allowed-headers: [Authorization, Content-Type, X-Requested-With]
    exposed-headers: [Authorization, Content-Type, Location]
    max-age: 3600

app:
  firebase:
    credentials-path: ${FIREBASE_CREDENTIALS:}
    project-id: ${FIREBASE_PROJECT_ID:}

springdoc:
  swagger-ui:
    enabled: false  # activé via application-dev.yml
```

Profils :
- `dev` : active swagger-ui (OpenApiConfig) ; utile en local
- default : swagger-ui désactivé

---

##  Endpoints principaux

Base : `/api`

### Public
- `GET /api/public/ping`  ping public, retourne `{ "status": "ok" }`

### Protégés (Bearer Token requis)
- `GET /api/auth/me`  profil courant
  - Requiert : `Authorization: Bearer <ID_TOKEN>`
  - Retour : `{ "uid": "...", "email": "...", "name": "...", "picture": "...", "roles": ["ROLE_USER"] }`
  - 401 si token manquant/invalide

---

##  Sécurité & flux

Pipeline de sécurité pour `/api/auth/me` :

1. **Preflight** : OPTIONS autorisé (CORS)
2. **CORS** : Appliqué via `CorsConfigurationSource`
3. **FirebaseTokenFilter** :
   - Lit `Authorization: Bearer <token>`
   - Vérifie via `firebaseAuth.verifyIdToken(token)`
   - Valide le provider (`google.com`, `microsoft.com` autorisés)
   - Construit l'`Authentication` avec roles `ROLE_USER`
4. **SecurityConfig** :
   - Stateless (`SessionCreationPolicy.STATELESS`)
   - `/api/public/**` et `/actuator/**` permis sans auth
   - Swagger-ui autorisé uniquement en profil `dev`

### Notes importantes
- Provider non autorisé  401 (requête non authentifiée)
- Email non vérifié avec `microsoft.com`  accepté mais logué en warning
- Token expiré ou invalide  401

---

##  Prérequis

* Java 17+
* Maven 3.8+
* Docker (optionnel)
* Compte Firebase et credentials (service account JSON)

---

##  Développement  démarrer localement

1. **Variables d'environnement** (exemples) :

```powershell
$env:FIREBASE_CREDENTIALS = "C:\\chemin\\firebase-sa.json"
$env:FIREBASE_PROJECT_ID = "littlebook-b2d2d"
$env:SPRING_PROFILES_ACTIVE = "dev"  # pour swagger-ui
```

2. Build :

```powershell
cd auth-service
mvn -DskipTests clean package
```

3. Run via Maven :

```powershell
mvn spring-boot:run
```

4. Ou exécuter le jar :

```powershell
java -jar target/auth-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

Le service démarre sur `http://localhost:8081`

Tests rapides :

```powershell
# Ping public
curl -i http://localhost:8081/api/public/ping

# Profil protégé (remplacer <ID_TOKEN> par un vrai token Firebase)
curl -i -H "Authorization: Bearer <ID_TOKEN>" http://localhost:8081/api/auth/me
```

---

##  Docker

Build :

```powershell
docker build -t littlebook-auth:local auth-service/
```

Run via Docker Compose :

```powershell
docker-compose up -d auth-service
```

Le service écoute sur le port `8081`

---

##  Tests

Lancer les tests :

```bash
mvn test
```

Couverture actuelle :
- **CorsPreflightTest** : Vérification préflights et en-têtes CORS
- **SecurityIntegrationTest** : Routes publiques/privées avec `@MockBean FirebaseAuth/FirebaseApp`

Ajouter selon besoin :
* Tests unitaires (FirebaseTokenFilter, provider validation)
* Tests d'intégration (flow complet authentification)
* Tests de charge (rate limiting)

---

##  Swagger / OpenAPI

Swagger UI (profil `dev` uniquement) :

```
http://localhost:8081/swagger-ui.html
```

OpenAPI JSON :

```
http://localhost:8081/v3/api-docs
```

 **Important** : Désactiver swagger-ui en production pour des raisons de sécurité

---

##  Production / bonnes pratiques

Configuration pour environnement de production :

*  Fournir `FIREBASE_CREDENTIALS` (ou ADC) et `FIREBASE_PROJECT_ID`
*  Ne pas activer profil `dev` (swagger-ui désactivé)
*  Stocker les secrets dans un secret manager (Vault, AWS KMS, GCP Secret Manager)
*  Ajouter rate limiting (Bucket4j) si exposé publiquement
*  Activer métriques/monitoring (Prometheus/Grafana via Actuator)
*  Logs structurés pour les accès (provider, email, statut)
*  HTTPS obligatoire (TLS 1.3)
*  CORS restrictif (limiter aux domaines frontend autorisés)

---

##  Fonctionnalités implémentées

-  **Validation Firebase ID token** : Support Google & Microsoft
-  **Endpoints REST** : Ping public + profil protégé
-  **Spring Security stateless** : FirebaseTokenFilter custom
-  **CORS configurable** : Configuration par propriétés
-  **Swagger UI** : Activable en profil `dev` uniquement
-  **Tests d'intégration** : MockBean Firebase
-  **Provider validation** : Whitelist google.com, microsoft.com
-  **Logging** : SLF4J avec contexte (provider, email)
-  **Stateless** : Scaling horizontal sans session

##  Perspectives futures

### Court terme (1-2 mois)
1. **Rate Limiting**  Protection contre brute-force avec Bucket4j
2. **Tests unitaires**  Coverage > 80% avec JUnit5 + Mockito
3. **Token refresh**  Endpoint pour rafraîchir les tokens expirés
4. **Admin endpoints**  Endpoints `/api/admin/*` avec `ROLE_ADMIN`

### Moyen terme (3-6 mois)
1. **Multi-provider**  Support Apple, GitHub, LinkedIn OAuth
2. **Token revocation**  Liste noire de tokens révoqués (Redis)
3. **Audit Trail**  Logging complet des authentifications (succès/échecs)
4. **Metrics**  Exposition Prometheus (auth rate, failures, latency)
5. **Circuit Breaker**  Resilience4j pour appels Firebase

### Long terme (6-12 mois)
1. **JWT issuing**  Émettre des JWT internes après validation Firebase
2. **Session management**  Support sessions pour clients non-SPA
3. **2FA**  Support authentification à deux facteurs
4. **Security events**  Webhooks sur événements de sécurité critiques
5. **Geographic restrictions**  Blocage par pays/région

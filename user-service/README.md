# user-service  Microservice Users

Ce dossier contient le microservice **user-service**, chargé de la gestion des utilisateurs au sein de LittleBook.
Il expose des opérations permettant de **créer**, **récupérer**, **mettre à jour** et **désactiver** des profils utilisateurs, notamment depuis les connexions OAuth (Google, Microsoft).

Ce README couvre uniquement le périmètre du microservice `user-service`.

---

##  Rôle du microservice

Le service fournit :

* Gestion des profils utilisateurs (CRUD complet)
* Synchronisation des comptes OAuth (Google, Microsoft)
* Gestion des rôles et permissions utilisateurs
* Soft-delete (désactivation de comptes)
* API REST exposée au front-end et aux autres microservices
* Base de données H2 en mémoire (dev), PostgreSQL prévu en production

Stack : Java 17, Spring Boot 3.x, Spring Data JPA, H2 Database.

---

##  Structure du projet

```
src/main/java/com/littlebook/user
  UserApplication.java             Entrypoint Spring Boot (port 8082)
  controller/
     UserController.java          Endpoints REST /user
  service/
     UserService.java             Logique métier utilisateurs
  repository/
     UserRepository.java          Spring Data JPA
  dto/
     CreateUserRequest.java       DTO création/OAuth
     UpdateUserRequest.java       DTO mise à jour partielle
     UserResponse.java            DTO de réponse
  entity/
     User.java                    Entité JPA User
  enums/
     Provider.java                Enum OAuth providers
  config/
     OpenApiConfig.java           Configuration Swagger

src/main/resources/
  application.yml                  Config (port 8082, H2 database)
  schema.sql                       Schéma de la base
  data.sql                         Données de test (user test)
Dockerfile                         Build Docker
```

---

##  Principales caractéristiques

* **Entrypoint** : `com.littlebook.user.UserApplication`
* **Port par défaut** : `8082`
* **API** : `/user/*`
* **Base de données** : H2 en mémoire pour développement
* **OAuth Providers** : Google, Microsoft (via Firebase)
* **Monitoring** : Swagger UI + Spring Actuator

---

##  Configuration (application.yml extrait)

```yaml
server:
  port: 8082

spring:
  application:
    name: user-service
  datasource:
    url: jdbc:h2:mem:userdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
  sql:
    init:
      mode: always  # Applique schema.sql et data.sql à chaque démarrage

app:
  firebase:
    credentials-path: ${FIREBASE_CREDENTIALS:}
    project-id: ${FIREBASE_PROJECT_ID:}
```

---

##  Endpoints principaux

Base : `/user`

### Health & Diagnostics
- `GET /user/health`  health check du service (retourne "user-service OK")
- `GET /user/ping`  ping simple (retourne "pong")

### Gestion des utilisateurs
- `POST /user/oauth`  Créer/synchroniser un profil OAuth
  - Body : `{ "provider": "GOOGLE", "providerId": "...", "email": "...", "name": "...", "picture": "...", "emailVerified": true }`
  - Retour : `UserResponse` (utilisateur créé ou mis à jour)
  
- `GET /user/{id}`  Récupérer un utilisateur par UUID
  - Retour : `UserResponse` ou 404

- `GET /user/by-email?email=...`  Récupérer un utilisateur par email
  - Retour : `UserResponse` ou 404

- `PATCH /user/{id}`  Mise à jour partielle du profil
  - Body : `{ "name": "...", "picture": "...", "roles": "ROLE_USER,ROLE_ADMIN" }`
  - Retour : `UserResponse` mis à jour

- `DELETE /user/{id}`  Désactivation (soft-delete)
  - Met `is_active=false`, retourne 204 No Content

---

##  Prérequis

* Java 17+
* Maven 3.8+
* Docker (optionnel)
* Compte Firebase et credentials (pour OAuth)

---

##  Développement  démarrer localement

1. Build :

```powershell
cd user-service
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

Le service démarre sur `http://localhost:8082`

Tests rapides :

```powershell
# Health check
curl -i http://localhost:8082/user/health

# Récupérer l'utilisateur de test (créé via data.sql)
curl "http://localhost:8082/user/by-email?email=test.user@example.com"

# Créer un utilisateur OAuth
curl -X POST "http://localhost:8082/user/oauth" -H "Content-Type: application/json" -d '{\"provider\":\"GOOGLE\",\"providerId\":\"google-123\",\"email\":\"new@example.com\",\"name\":\"Test User\",\"picture\":\"https://example.com/avatar.png\",\"emailVerified\":true}'
```

---

##  Docker

Build :

```powershell
docker build -t littlebook-user:local user-service/
```

Run via Docker Compose :

```powershell
docker-compose up -d user-service
```

Le service écoute sur le port `8082`

---

##  Base de données

* **Type** : H2 en mémoire pour le développement
* **Configuration** : `src/main/resources/application.yml`
* **URL** : `jdbc:h2:mem:userdb`
* **Console** : Disponible à `http://localhost:8082/h2-console` (si activée)
* **DDL** : Hibernate `ddl-auto: none` (schéma géré par `schema.sql`)
* **Initialisation** : `spring.sql.init.mode=always`  schéma et données rechargés à chaque démarrage

### Données de test
- **Fichier schéma** : `src/main/resources/schema.sql` (table `users`)
- **Fichier données** : `src/main/resources/data.sql`
  - Utilisateur test : `test.user@example.com` (UUID: `11111111-1111-1111-1111-111111111111`)`

---

##  Tests

Lancer les tests :

```bash
mvn test
```

Ajouter selon besoin :

* tests unitaires (services, mapping DTO)
* tests d'intégration (WebTestClient / MockMvc)
* tests de validation OAuth (mock Firebase)

---

##  Sécurité

Configuration basique pour le développement. Avant production :

* Ajouter authentification JWT (intégration avec auth-service)
* Valider les appels inter-services (HTTPS, mTLS)
* Restreindre les endpoints selon les rôles (ROLE_ADMIN, ROLE_USER)
* Chiffrer les données sensibles (email, picture)
* Activer CORS restrictif (uniquement domaines frontend autorisés)

---

##  Swagger / OpenAPI

Disponible via springdoc :

```
http://localhost:8082/swagger-ui.html
```

OpenAPI JSON :

```
http://localhost:8082/v3/api-docs
```

La documentation interactive affiche tous les endpoints avec les modèles de données.

---

##  Fonctionnalités implémentées

-  **CRUD complet** : Create, Read, Update, Soft-Delete avec validation
-  **OAuth Sync** : Synchronisation Google & Microsoft via Firebase
-  **Soft-delete** : Désactivation réversible (`is_active=false`)
-  **Gestion des rôles** : Support ROLE_USER, ROLE_ADMIN
-  **H2 en mémoire** : Démarrage rapide avec données de test
-  **Validation** : Jakarta Validation annotations sur DTOs
-  **Swagger/OpenAPI** : Documentation interactive complète
-  **Health Checks** : Endpoints `/user/health` et `/user/ping`
-  **Logging** : SLF4J avec messages structurés

##  Perspectives futures

### Court terme (1-2 mois)
1. **JWT Authentication**  Intégration avec auth-service pour validation tokens
2. **Tests unitaires**  Coverage > 80% avec JUnit5 + Mockito
3. **Pagination**  Support `page`, `size`, `sort` sur GET /user
4. **Validation avancée**  Règles métier (email format, force mot de passe)

### Moyen terme (3-6 mois)
1. **PostgreSQL**  Migration vers PostgreSQL en production
2. **Redis Caching**  Cache des profils fréquemment consultés
3. **Permissions granulaires**  RBAC avancé avec permissions custom
4. **User Preferences**  Gestion des préférences (notifications, thème, langue)
5. **Audit Trail**  Historique complet des modifications de profil

### Long terme (6-12 mois)
1. **Multi-tenancy**  Support de plusieurs organisations
2. **GDPR Compliance**  Export/suppression données utilisateur
3. **Social Features**  Avatar upload, bio, liens sociaux
4. **Activity Tracking**  Suivi de l'activité utilisateur (connexions, actions)
5. **GraphQL API**  Alternative à REST pour requêtes complexes

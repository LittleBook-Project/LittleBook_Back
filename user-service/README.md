# user-service

Ce README explique comment lancer le `user-service` en local, quelles routes REST sont exposées, et où trouver la documentation Swagger/OpenAPI.

Le service utilise une base H2 en mémoire pour le développement. À chaque démarrage la base est recréée à partir de `schema.sql` et pré-remplie avec un utilisateur de test défini dans `data.sql` (email: `test.user@example.com`).

Port par défaut
- http://localhost:8082

Prérequis
- JDK 17
- Maven 3.6+
- (Optionnel) Docker si vous préférez Postgres en local

Lancer le service

Depuis le dossier `user-service` :

```powershell
mvn spring-boot:run
```

Après démarrage, l'API écoute sur le port 8082. La DB H2 en mémoire est initialisée automatiquement par `schema.sql` et `data.sql`.

Endpoints principaux

Base: `/user`

- GET `/user/health` — simple health check (retourne `user-service OK`)
- GET `/user/ping` — ping → `pong`
- POST `/user/oauth` — Provision / synchronise un profil venant d'un provider OAuth (appelé depuis l'`auth-service`).
  - Payload (JSON) :
    ```json
    {
      "provider": "GOOGLE",
      "providerId": "google-1234567890",
      "email": "test.user@example.com",
      "name": "Test User",
      "picture": "https://example.com/avatar.png",
      "emailVerified": true
    }
    ```
  - Retour : `UserResponse` (objet utilisateur créé / mis à jour)

- GET `/user/{id}` — récupère le profil par UUID
- GET `/user/by-email?email=...` — récupère le profil par email
- PATCH `/user/{id}` — met à jour partiellement le profil
  - Payload (JSON) :
    ```json
    {
      "name": "Nouvel Nom",
      "picture": "https://example.com/new.png",
      "roles": "ROLE_USER,ROLE_ADMIN"
    }
    ```
  - Retour : `UserResponse` mis à jour

- DELETE `/user/{id}` — soft-delete (set `is_active=false`), retourne 204 No Content

Exemples curl (PowerShell)

```powershell
# 1) Récupérer l'utilisateur de test
curl "http://localhost:8082/user/by-email?email=test.user@example.com"

# 2) Créer / synchroniser un utilisateur OAuth
curl -X POST "http://localhost:8082/user/oauth" -H "Content-Type: application/json" -d '{"provider":"GOOGLE","providerId":"google-abc","email":"new@example.com","name":"New","picture":"https://...","emailVerified":true}'

# 3) Mettre à jour un utilisateur (patch)
curl -X PATCH "http://localhost:8082/user/11111111-1111-1111-1111-111111111111" -H "Content-Type: application/json" -d '{"name":"Test Updated"}'
```

Swagger / OpenAPI

- Swagger UI (interface web) :
  - http://localhost:8082/swagger-ui.html
  - ou http://localhost:8082/swagger-ui/index.html
- OpenAPI JSON : http://localhost:8082/v3/api-docs

Si tu ne vois pas Swagger, vérifie `SecurityConfig` : les chemins `/v3/api-docs/**` et `/swagger-ui/**` doivent être autorisés en développement.

Base de données (dev)
- Fichier de schéma : `src/main/resources/schema.sql`
- Fichier d'initialisation de données : `src/main/resources/data.sql` (contient l'utilisateur de test `test.user@example.com`)
- Configuration d'initialisation : `spring.sql.init.mode=always` dans `application.yml` (le schéma et les données sont ré-appliqués à chaque démarrage — idéal pour les tests locaux)

Passer en Postgres (optionnel)
- Le driver PostgreSQL est déjà présent dans le `pom.xml`.
# user-service (court)

Lancement

```powershell
cd user-service
mvn spring-boot:run
```

Base: http://localhost:8082

Endpoints principaux (base `/user`)
- GET `/user/health` — health check
- GET `/user/ping` — ping
- POST `/user/oauth` — provisione / synchronise un profil OAuth (payload JSON: provider, providerId, email, name, picture, emailVerified)
- GET `/user/{id}` — récupère par UUID
- GET `/user/by-email?email=...` — récupère par email
- PATCH `/user/{id}` — met à jour partiellement (name, picture, roles)
- DELETE `/user/{id}` — soft-delete (is_active=false)

Swagger / OpenAPI
- UI : http://localhost:8082/swagger-ui.html (ou /swagger-ui/index.html)
- Spec : http://localhost:8082/v3/api-docs

Base de données (dev)
- H2 en mémoire ; initialisée à chaque démarrage par `src/main/resources/schema.sql` et `src/main/resources/data.sql`.
- Utilisateur de test : `test.user@example.com` (id `11111111-1111-1111-1111-111111111111`)

Fin.

```bash
mvn spring-boot:run
```
---


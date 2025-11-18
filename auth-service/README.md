# Auth Service — LittleBook

Service d'authentification indépendant (SOA) pour LittleBook.

Ce README décrit le service, son architecture, son déploiement local/production, les endpoints exposés, la sécurité, les tests et les recommandations opérationnelles.

---

## 1. Présentation

- Rôle : service autonome de validation d'ID tokens Firebase. Il fournit des endpoints REST permettant aux frontends de vérifier l'identité d'un utilisateur et d'obtenir des informations de profil.
- Contexte technologique : Java 17, Spring Boot 3.3.x, Spring Security 6, Firebase Admin SDK, springdoc OpenAPI.
- Caractéristiques : stateless (pas de base de données), s'intègre avec Firebase pour valider les tokens émis par Google Sign-In.

Pourquoi stateless ?
- Le service ne conserve aucun état utilisateur côté serveur : il valide des tokens fournis par le client et retourne des informations extraites du token. Cela facilite le scaling horizontal et rend le service simple à déployer.

## 2. Fonctionnalités

- Vérification et décodage d'ID token Firebase via `FirebaseAuth.verifyIdToken()`.
- Endpoints REST :
  - `GET /api/public/ping` — endpoint public pour vérifier la disponibilité.
  - `GET /api/auth/me` — endpoint protégé qui retourne le profil extrait du token (uid, email, name, picture, roles).
- Sécurité : Spring Security configuré en mode stateless ; authentification via un filtre custom `FirebaseTokenFilter` qui extrait et vérifie le Bearer token.
- CORS : contrôlé par propriétés (`security.cors.*`) et exposé via un `CorsConfigurationSource` intégré à la chaîne de sécurité.
- Documentation OpenAPI/SWAGGER : fournie par `springdoc`; UI activée uniquement en profil `dev`.

## 3. Architecture (schéma texte)

Pipeline d'une requête protégée (/api/auth/me) :

Client -> (préflight CORS possible) -> Tomcat -> Spring Security FilterChain
  -> CorsConfigurationSource (vérifie origin/method/headers)
  -> FirebaseTokenFilter (si route non publique/OPTIONS)
       - lit header `Authorization: Bearer <ID_TOKEN>`
       - appelle `FirebaseAuth.verifyIdToken(token)`
       - sur succès : remplit SecurityContext avec Authentication (uid + détails)
       - sur échec : ne met pas d'authentification (entrée renvoie 401 par EntryPoint)
  -> Controller `AuthController` (lit Authentication et retourne profil)

Composants principaux :
- `FirebaseConfig` : initialisation `FirebaseApp` (fichier credentials ou ADC)
- `FirebaseTokenFilter` : filtre de vérification des tokens
- `SecurityConfig` : configuration Spring Security (STATELESS, exception handling, autorisations)
- `CorsConfig` + `CorsProperties` : configuration CORS centralisée
- `AuthController` : endpoints /api/public/ping et /api/auth/me

## 4. Installation & Exécution

Prerequis :
- Java 17+ installé
- Maven 3.8+

Variables d'environnement (exigées / recommandées) :
- `FIREBASE_CREDENTIALS` : chemin absolu vers le JSON du service account (optionnel si ADC utilisé)
- `FIREBASE_PROJECT_ID` : (optionnel) project id Firebase
- `SPRING_PROFILES_ACTIVE` : `dev` pour activer Swagger UI (optionnel)

Exemples (PowerShell) :

```powershell
# définir les variables (Windows PowerShell)
$env:FIREBASE_CREDENTIALS = "C:\Users\mathis\OneDrive - Universite Evry Val d'Essonne\M2\projet1\front\littlebook.json"
$env:FIREBASE_PROJECT_ID = 'my-firebase-project'
$env:SPRING_PROFILES_ACTIVE = 'dev'   # active swagger-ui

# lancer en développement
cd auth-service
mvn spring-boot:run
```

Builder puis lancer le jar :

```powershell
mvn -f auth-service/pom.xml -DskipTests package
java -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

Fichier `application.yml` (extrait) :

```yaml
server:
  port: 8081

security:
  cors:
    allowed-origins:
      - "http://127.0.0.1:5500"
      - "http://localhost:5500"
      - "http://localhost:5173"
    allowed-methods: [GET,POST,PUT,PATCH,DELETE,OPTIONS]
    allowed-headers: [Authorization,Content-Type,X-Requested-With]
    exposed-headers: [Authorization,Content-Type,Location]

app:
  firebase:
    credentials-path: ${FIREBASE_CREDENTIALS:}
    project-id: ${FIREBASE_PROJECT_ID:}

springdoc:
  swagger-ui:
    enabled: false   # activé via application-dev.yml
```

⚠️ À compléter : gérer les secrets via Vault / KMS en prod.

## 5. Endpoints

1) GET /api/public/ping
- Description : point de test public
- Exemple :
  ```bash
  curl -i http://localhost:8081/api/public/ping
  ```
- Réponse 200 :
  ```json
  { "status": "ok" }
  ```

2) GET /api/auth/me
- Description : retourne le profil de l'utilisateur authentifié via le Firebase ID token
- Requête : header `Authorization: Bearer <ID_TOKEN>`
- Exemple (sans token) :
  ```bash
  curl -i http://localhost:8081/api/auth/me
  # => 401 Unauthorized
  ```
- Exemple (avec token) :
  ```bash
  curl -i -H "Authorization: Bearer <ID_TOKEN>" http://localhost:8081/api/auth/me
  ```
- Réponse 200 :
  ```json
  {
    "uid": "uid123",
    "email": "john@doe.com",
    "name": "John Doe",
    "picture": "https://...",
    "roles": ["ROLE_USER"]
  }
  ```
- Codes d'erreur :
  - 401 Unauthorized : token manquant ou invalide
  - 403 Forbidden : accès refusé (rare ici, route protégée sans permission)

## 6. Sécurité

- Bearer token : le filtre `FirebaseTokenFilter` lit `Authorization` et cherche `Bearer `.
- Vérification : `firebaseAuth.verifyIdToken(token)` — en cas d'exception, la requête n'est pas authentifiée.
- `SessionCreationPolicy.STATELESS` : justification
  - Le serveur n'a pas à conserver d'état : tout s'appuie sur le token signé par Firebase. Cela permet un scaling horizontal simple et évite la charge mémoire/stockage côté serveur.
- CORS :
  - Les origines autorisées sont définies dans `application.yml` (`security.cors.allowed-origins`)
  - Le bean `CorsConfigurationSource` est utilisé par Spring Security (garantit cohérence entre CORS et sécurité)

## 7. Déploiement

Build JAR :
```bash
mvn -f auth-service/pom.xml -DskipTests package
```

Variables à fournir en production :
- `FIREBASE_CREDENTIALS` (ou utilisez ADC via `GOOGLE_APPLICATION_CREDENTIALS`),
- `FIREBASE_PROJECT_ID`,
- `SPRING_PROFILES_ACTIVE` (ne PAS activer `dev` en prod). 

Recommandations prod :
- Ne pas exposer Swagger UI en production.
- Utiliser un secret manager (Vault / AWS KMS / GCP Secret Manager) pour stocker `firebase-sa.json` ou utiliser ADC.
- Ajouter rate-limiting (ex: Bucket4j) si le service est exposé publiquement.
- Configurer logs structurés et monitoring (Prometheus / Grafana) via Actuator metrics.

## 8. Tests

Exécuter la suite :
```bash
mvn -f auth-service/pom.xml test
```

Tests inclus :
- `CorsPreflightTest` : vérifie les préflights OPTIONS et les en-têtes CORS.
- `SecurityIntegrationTest` : tests d'intégration mockant `FirebaseAuth` pour couvrir :
  - route publique accessible,
  - route protégée renvoyant 401 sans token,
  - route protégée renvoyant 200 avec token mocké,
  - comportement en cas de token invalide.

Comment mocker Firebase Admin pour les tests :
- Les tests utilisent `@MockBean` pour `FirebaseAuth` et `FirebaseApp` (voir `SecurityIntegrationTest`). Ainsi la validation de token est simulée et les tests sont déterministes.



## 9. FAQ & Conseils

- Pourquoi pas de base de données ?
  - Le service ne stocke aucun état : il se contente de vérifier des tokens signés par Firebase et retourne les informations. Ajouter une DB serait nécessaire uniquement si on veut garder des sessions, logs personnalisés ou lier des profils à des données internes.

- Ajouter d'autres providers OAuth ?
  - Abstraire la vérification dans un service `TokenVerificationService` et fournir des implémentations pour Firebase, Auth0, etc. Le filtre pourrait déléguer à ce service.

- Étendre les endpoints
  - Ajouter `/api/auth/refresh` si l'on souhaite gérer des refresh tokens côté serveur (nécessite stockage et revocation logiciel).

## 10. Licence & auteurs

Projet LittleBook — licence : voir `LICENSE` à la racine du dépôt.

---

⚠️ À compléter / recommandations futures
- Ajouter `application-prod.yml` et verrouiller Actuator/Swagger.
- Ajouter un `.env.example` (variables d'environnement obligatoires) — recommandé pour nouveaux contributeurs.
- Ajouter protection Basic Auth pour Swagger UI en dev si nécessaire.

Si tu veux, je peux :
- créer `auth-service/README.md` (fait),
- ajouter `.env.example` automatiquement,
- sécuriser Swagger UI par BasicAuth en dev,
- créer un script `smoke-test.ps1`.

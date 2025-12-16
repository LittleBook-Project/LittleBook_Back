# gateway  API Gateway (Monolithe en transition)

Ce dossier contient le **gateway**, actuellement un **monolithe regroupant tous les domaines métier** de LittleBook.
Il expose les fonctionnalités principales : utilisateurs, livres, reviews, abonnements et listes de lecture via une API REST unique.

**Rôle de transition** : Ce monolithe sert de point d'entrée unique sur le port 8080 en attendant la migration complète vers les microservices dédiés.

Ce README couvre uniquement le périmètre du gateway.

---

##  Rôle du gateway

Le service fournit :

* Point d'accès HTTP unique sur `/api/**` pour tous les domaines métier
* Exposition des domaines : Users, Books, Reviews, Subscriptions, Reading lists
* Connexion à PostgreSQL (Supabase) pour la persistance
* API REST exposée au front-end
* Base de code monolithique en cours de découpage vers microservices

Stack : Java 17, Spring Boot 3.x, Spring Security (basique), Spring Data JPA, PostgreSQL.

---

##  Structure du projet

```
src/main/java/com/littlebook
  LittlebookApplication.java           Entrypoint Spring Boot (port 8080)
  controller/                          REST controllers
     UserController.java              /api/users (2 endpoints)
     BookController.java              /api/books (5 endpoints)
     ReviewController.java            /api/reviews (7 endpoints)
     SubscriptionController.java      /api/subscriptions (6 endpoints)
     ReadingController.java           /api/reading (10 endpoints)
  service/                             Logique métier (User, Book, Review, Subscription, Reading)
  repository/                          Spring Data JPA
  entity/                              Entités JPA (User, Book, Review, Reading, Subscription)
  dto/                                 DTOs (ReviewDTO, ReadingDTO, SubscriptionDTO...)
  enums/                               ReadingStatus, Roles...
  config/
     SecurityConfig.java              Configuration sécurité
     CorsConfig.java                  Configuration CORS

src/main/resources/application.yml        Config (port 8080, datasource Postgres)
Dockerfile                                Build Docker
```

---

##  Principales caractéristiques

* **Entrypoint** : `com.littlebook.LittlebookApplication`
* **Port par défaut** : `8080`
* **API** : `/api/**` (tous domaines)
* **Base de données** : PostgreSQL via Supabase
* **Architecture** : Monolithe (migration microservices en cours)
* **État actuel** : Point d'entrée unique, fonctionnalités complètes

---

##  Configuration (application.yml extrait)

```yaml
server:
  port: 8080

spring:
  application:
    name: gateway
  datasource:
    url: jdbc:postgresql://db.epgxaozzqwdlbwtuefye.supabase.co:5432/postgres?sslmode=require
    username: postgres
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

app:
  firebase:
    credentials-path: ${FIREBASE_CREDENTIALS:}
    project-id: ${FIREBASE_PROJECT_ID:}
```

---

##  Endpoints principaux (résumé)

### Users (`/api/users`)
- `POST /register`  créer un utilisateur
- `GET /{userName}`  récupérer un utilisateur

### Books (`/api/books`)
- `GET /{isbn}`  récupérer un livre par ISBN
- `GET /search/title?title=`  recherche par titre
- `GET /search/author?author=`  recherche par auteur
- `POST /`  créer/mettre à jour un livre
- `GET /test-openlibrary/{isbn}`  test de lookup OpenLibrary

### Reviews (`/api/reviews`)
- `POST /`  créer une review
- `PUT /{reviewId}`  mettre à jour une review
- `DELETE /{reviewId}?userUuid=`  supprimer une review
- `GET /{reviewId}`  récupérer une review
- `GET /book/{isbn}`  reviews d'un livre
- `GET /book/{isbn}/stats`  stats d'un livre (note moyenne, nombre)
- `GET /user/{userUuid}`  reviews d'un utilisateur

### Subscriptions (`/api/subscriptions`)
- `POST /`  s'abonner à un utilisateur
- `DELETE /{subscriberUuid}/{subscribedToUuid}`  se désabonner
- `GET /{subscriberUuid}/following`  liste suivis
- `GET /{subscribedToUuid}/followers`  liste abonnés
- `GET /{subscriberUuid}/follows/{subscribedToUuid}`  vérifie le follow
- `GET /{userUuid}/stats`  stats abonnements

### Reading lists (`/api/reading`)
- `POST /`  ajouter un livre à la liste
- `PUT /{userUuid}/{bookIsbn}`  changer le statut (TO_READ, READING, READ)
- `DELETE /{userUuid}/{bookIsbn}`  retirer de la liste
- `GET /user/{userUuid}`  toutes les lectures
- `GET /user/{userUuid}/status/{status}`  lectures par statut
- `GET /user/{userUuid}/to-read|currently-reading|read`  filtres rapides
- `GET /{userUuid}/{bookIsbn}/status`  statut d'un livre
- `GET /{userUuid}/{bookIsbn}/exists`  présence dans la liste
- `GET /user/{userUuid}/stats`  stats de lecture

---

##  Prérequis

* Java 17+
* Maven 3.8+
* Docker (optionnel)
* Accès PostgreSQL Supabase

---

##  Développement  démarrer localement

1. **Variables d'environnement** :

```powershell
$env:DB_PASSWORD = "Need2eatandsleep@diner"
$env:FIREBASE_CREDENTIALS = "C:\\chemin\\littlebook.json"
$env:FIREBASE_PROJECT_ID = "littlebook-b2d2d"
```

2. Build :

```powershell
cd gateway
mvn -DskipTests clean package
```

3. Run via Maven :

```powershell
mvn spring-boot:run
```

4. Ou exécuter le jar :

```powershell
java -jar target/*.jar
```

Le service démarre sur `http://localhost:8080`

Tests rapides :

```powershell
# Test connexion Supabase
curl -i http://localhost:8080/api/users/testuser

# Test création livre
curl -X POST http://localhost:8080/api/books -H "Content-Type: application/json" -d '{"isbn":"9780439708180","title":"Harry Potter"}'
```

---

##  Docker

Build :

```powershell
docker build -t littlebook-gateway:local gateway/
```

Run via Docker Compose :

```powershell
docker-compose up -d gateway
```

Le service écoute sur le port `8080`

 **Important** : Variables d'environnement requises (DB_PASSWORD, FIREBASE_CREDENTIALS)

---

##  Base de données

* **Type** : PostgreSQL (Supabase)
* **Configuration** : `src/main/resources/application.yml`
* **URL** : `jdbc:postgresql://db.epgxaozzqwdlbwtuefye.supabase.co:5432/postgres`
* **DDL** : Hibernate `ddl-auto: none` (schéma géré manuellement)
* **Migrations** : Scripts SQL dans `src/main/resources/` (si présents)

### Entités principales
- **User** : Profils utilisateurs
- **Book** : Catalogue de livres
- **Review** : Avis et notes sur livres
- **Subscription** : Abonnements entre utilisateurs
- **Reading** : Listes de lecture avec statuts

---

##  Tests

Lancer les tests :

```bash
mvn test
```

Ajouter selon besoin :
* Tests unitaires (services, mapping DTO)
* Tests d'intégration (WebTestClient / MockMvc)
* Tests sur connexion Supabase

---

##  Sécurité

Configuration basique pour le développement. Avant production :

* Ajouter authentification (Firebase/JWT) et propagation des rôles
* Restreindre les endpoints selon les permissions
* CORS restrictif (limiter aux domaines frontend autorisés)
* Sécuriser secrets (DB_PASSWORD, FIREBASE_*) via vault/secrets manager
* HTTPS obligatoire (TLS 1.3)
* Rate limiting global

---

##  Swagger / OpenAPI

Swagger UI à activer (OpenApiConfig) :

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON :

```
http://localhost:8080/v3/api-docs
```

---

##  Architecture & Migration

### État actuel : Monolithe
- Tous les domaines métier dans un seul service
- Base de données PostgreSQL unique
- Point d'entrée unique sur port 8080

### Microservices existants (déjà extraits)
-  **auth-service** (8081) : Authentification Firebase
-  **user-service** (8082) : Gestion utilisateurs (H2)
-  **review-service** (8083) : Reviews (H2, à implémenter)
-  **book-service** (8084) : Catalogue + OpenLibrary (H2)
-  **admin-service** (8085) : Administration + stats (H2)

### Domaines restants à extraire
-  **subscription-service** : Abonnements entre utilisateurs
-  **reading-service** : Listes de lecture avec statuts

### Cible : API Gateway routeur
```
Frontend  Gateway (routing seulement)
            
             auth-service (8081)
             user-service (8082)
             review-service (8083)
             book-service (8084)
             admin-service (8085)
             subscription-service (8086) [à créer]
             reading-service (8087) [à créer]
```

---

##  Fonctionnalités implémentées

-  **Gestion utilisateurs** : Création, lookup
-  **Catalogue livres** : Lookup, recherche, création
-  **Reviews** : CRUD, stats
-  **Abonnements** : Follow/unfollow, stats
-  **Listes de lecture** : Ajout, statut, stats
-  **PostgreSQL** : Persistance via Spring Data JPA
-  **API REST** : 35+ endpoints sur port 8080

##  Perspectives futures

### Court terme (1-2 mois)
1. **Extraire Subscriptions**  Créer subscription-service (port 8086)
2. **Extraire Reading**  Créer reading-service (port 8087)
3. **Tests d'intégration**  Coverage > 70% sur gateway
4. **JWT Authentication**  Intégration avec auth-service

### Moyen terme (3-6 mois)
1. **Transformer en routeur**  Supprimer logique métier, garder routing uniquement
2. **Spring Cloud Gateway**  Remplacer monolithe par vrai API Gateway
3. **Circuit Breakers**  Resilience4j pour tolérance aux pannes microservices
4. **Service Discovery**  Eureka ou Consul pour découverte dynamique
5. **Distributed Tracing**  Zipkin/Jaeger pour monitoring distribué

### Long terme (6-12 mois)
1. **Load Balancing**  Plusieurs instances par microservice
2. **API Rate Limiting**  Bucket4j au niveau gateway
3. **GraphQL Gateway**  Alternative REST pour requêtes complexes
4. **Event-Driven**  Kafka/RabbitMQ pour communication asynchrone
5. **Service Mesh**  Istio pour gestion avancée du trafic

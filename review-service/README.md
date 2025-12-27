# review-service  Microservice Reviews

Ce dossier contient le microservice **review-service**, chargé de la gestion des reviews/avis de livres au sein de LittleBook.
Il expose des opérations permettant de **créer**, **lire**, **modifier** et **supprimer** des reviews utilisateurs (CRUD à implémenter).

Ce README couvre uniquement le périmètre du microservice `review-service`.

---

##  Rôle du microservice

Le service fournit :

* Gestion des reviews de livres (CRUD complet à implémenter)
* Calcul de statistiques par livre (note moyenne, nombre de reviews)
* Liaison avec user-service et book-service (future communication inter-services)
* API REST exposée au front-end et aux autres microservices
* Base de données H2 en mémoire (dev), PostgreSQL prévu en production

Stack : Java 17, Spring Boot 3.x, Spring Data JPA, H2 Database.

---

##  Structure du projet

```
src/main/java/com/littlebook/review
  ReviewApplication.java           Entrypoint Spring Boot (port 8083)
  controller/
     ReviewController.java        Endpoints REST /review
  service/
     ReviewService.java           Logique métier reviews (à implémenter)
  repository/
     ReviewRepository.java        Spring Data JPA
  dto/
     ReviewDTO.java               DTOs de transfert
  entity/
     Review.java                  Entité JPA Review
  config/
      OpenApiConfig.java           Configuration Swagger (si présent)

src/main/resources/application.yml    Config (port 8083, H2 database)
Dockerfile                             Build Docker
```

---

##  Principales caractéristiques

* **Entrypoint** : `com.littlebook.review.ReviewApplication`
* **Port par défaut** : `8083`
* **API** : `/review/*`
* **Base de données** : H2 en mémoire pour développement
* **État actuel** : Health checks implémentés, CRUD à venir
* **Monitoring** : Swagger UI (à activer)

---

##  Configuration (application.yml extrait)

```yaml
server:
  port: 8083

spring:
  application:
    name: review-service
  datasource:
    url: jdbc:h2:mem:reviewdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

app:
  firebase:
    credentials-path: ${FIREBASE_CREDENTIALS:}
    project-id: ${FIREBASE_PROJECT_ID:}
```

---

##  Endpoints principaux

Base : `/review`

### Health & Diagnostics
- `GET /review/health`  health check du service
- `GET /review/ping`  ping simple (retourne "pong")

### Reviews (à implémenter)
- `POST /review`  créer une review
  - Body : `{ "bookIsbn": "...", "userUuid": "...", "rating": 4, "comment": "..." }`
  - Retour : `ReviewDTO` créée
  
- `GET /review/{id}`  récupérer une review par ID
  - Retour : `ReviewDTO` ou 404

- `PUT /review/{id}`  modifier une review
  - Body : `{ "rating": 5, "comment": "Excellent!" }`
  - Retour : `ReviewDTO` mise à jour

- `DELETE /review/{id}`  supprimer une review
  - Retour : 204 No Content

- `GET /review/book/{isbn}`  toutes les reviews d'un livre
  - Retour : `List<ReviewDTO>`

- `GET /review/user/{userUuid}`  toutes les reviews d'un utilisateur
  - Retour : `List<ReviewDTO>`

- `GET /review/book/{isbn}/stats`  statistiques d'un livre
  - Retour : `{ "averageRating": 4.2, "totalReviews": 15 }`

---

##  Prérequis

* Java 17+
* Maven 3.8+
* Docker (optionnel)
* Accès réseau pour communication inter-services

---

##  Développement  démarrer localement

1. Build :

```powershell
cd review-service
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

Le service démarre sur `http://localhost:8083`

Tests rapides :

```powershell
# Health check
curl -i http://localhost:8083/review/health

# Ping
curl -i http://localhost:8083/review/ping
```

---

##  Docker

Build :

```powershell
docker build -t littlebook-review:local review-service/
```

Run via Docker Compose :

```powershell
docker-compose up -d review-service
```

Le service écoute sur le port `8083`

---

##  Base de données

* **Type** : H2 en mémoire pour le développement
* **Configuration** : `src/main/resources/application.yml`
* **URL** : `jdbc:h2:mem:reviewdb`
* **Console** : Disponible à `http://localhost:8083/h2-console` (si activée)
* **DDL** : Hibernate `ddl-auto: update` (crée les tables automatiquement)

### Entités prévues
- **Review** : id, bookIsbn, userUuid, rating, comment, createdAt, updatedAt

---

##  Tests

Lancer les tests :

```bash
mvn test
```

Ajouter selon besoin :

* tests unitaires (services, mapping DTO)
* tests d'intégration (WebTestClient / MockMvc)
* tests de validation (rating 1-5, champs requis)

---

##  Sécurité

Configuration basique pour le développement. Avant production :

* Ajouter authentification JWT (intégration avec auth-service)
* Valider ownership des reviews (seul l'auteur peut modifier/supprimer)
* Rate limiting pour éviter spam de reviews
* Validation stricte des ratings (1-5)
* Modération de contenu (détection langage offensant)

---

##  Swagger / OpenAPI

Swagger UI à activer (OpenApiConfig) :

```
http://localhost:8083/swagger-ui.html
```

OpenAPI JSON :

```
http://localhost:8083/v3/api-docs
```

La documentation interactive affichera tous les endpoints une fois le CRUD implémenté.

---

##  Communication Inter-Services

### Intégrations prévues
- **book-service** : Vérifier l'existence du livre avant création de review
- **user-service** : Valider l'existence de l'utilisateur
- **Notifications** : Notifier l'auteur du livre lors d'une nouvelle review

### RestTemplate / WebClient
Configuration à ajouter dans `RestTemplateConfig.java` ou `WebClientConfig.java`

---

##  Fonctionnalités implémentées

-  **Health check endpoints** : `/review/health`, `/review/ping`
-  **H2 database** : Configuration en mémoire prête
-  **Structure projet** : Controllers, services, repositories
-  **CRUD reviews** : À implémenter
-  **Statistiques** : Calcul note moyenne, count
-  **Inter-services** : Communication avec book-service et user-service

##  Perspectives futures

### Court terme (1-2 mois)
1. **CRUD complet**  Implémenter tous les endpoints de gestion des reviews
2. **Validation**  Rating 1-5, champs requis, longueur comment
3. **Tests unitaires**  Coverage > 80% avec JUnit5 + Mockito
4. **Inter-services**  Vérification book/user via RestTemplate

### Moyen terme (3-6 mois)
1. **PostgreSQL**  Migration vers PostgreSQL en production
2. **Pagination**  Support `page`, `size`, `sort` sur les listes
3. **Filtres avancés**  Recherche par rating, date, mots-clés
4. **Modération**  Système de signalement et validation manuelle
5. **Agrégations**  Stats avancées (tendances, top reviewers)

### Long terme (6-12 mois)
1. **Machine Learning**  Détection automatique de spam/fake reviews
2. **Sentiment Analysis**  Analyse du sentiment des commentaires
3. **Recommendations**  Suggestions de livres basées sur reviews
4. **Media Upload**  Support images dans les reviews
5. **Social Features**  Votes utiles, réponses aux reviews

Voici un **README dédié au microservice `book-service`**, cohérent avec le style et le niveau de détail du README admin, et adapté à votre découpage microservices + usages OpenLibrary.

---

# book-service — Microservice Books

Ce dossier contient le microservice **book-service**, chargé de la gestion des livres au sein de LittleBook.
Il expose des opérations permettant de **créer**, **récupérer**, **chercher** et **enrichir** des livres, notamment grâce à des appels externes vers **OpenLibrary**.

Ce README couvre uniquement le périmètre du microservice `book-service`.

---

## 📘 Rôle du microservice

Le service fournit :

* Création et récupération des entités Livre (DB locale ou cache)
* Recherche de livres par titre, auteur ou ISBN
* Enrichissement depuis **OpenLibrary API** (couverture, résumé, métadonnées)
* Normalisation des données entrantes / sortantes
* API REST exposée au front-end et aux autres microservices (ex : review-service)

---

## 🏗️ Structure du projet

```
src/main/java/com/littlebook/book
 ├── BookApplication.java        → Entrypoint Spring Boot
 ├── config/
 │   ├── OpenApiConfig.java      → Configuration Swagger/OpenAPI
 │   └── SecurityConfig.java     → Configuration de sécurité
 ├── controller/
 │   └── BookController.java     → Endpoints REST
 ├── service/
 │   ├── BookService.java        → Logique métier (CRUD)
 │   ├── BookSyncService.java    → Synchronisation OpenLibrary
 │   └── external/
 │       └── OpenLibraryClient.java  → Client HTTP pour OpenLibrary
 ├── dto/
 │   ├── BookRequest.java        → DTO pour créer/modifier
 │   ├── BookResponse.java       → DTO pour les réponses
 │   └── openlibrary/            → DTOs OpenLibrary
 ├── entity/
 │   └── BookEntity.java         → Entité JPA
 ├── repository/
 │   └── BookRepository.java     → Accès à la DB
 └── exception/
     ├── BookNotFoundException.java
     ├── OpenLibraryException.java
     ├── GlobalExceptionHandler.java
     └── ErrorResponse.java

src/main/resources/application.yml      → Configuration
Dockerfile                               → Build Docker
```

---

## 🚀 Principales caractéristiques

* **Entrypoint** : `com.littlebook.book.BookApplication`
* **Port par défaut** : `8084`
* **API** : `/books/*`
* **Client HTTP** pour OpenLibrary (RestClient Spring 6 natif)
* **DB** : H2 en mémoire pour développement
* **Synchronisation** : Intégration complète avec OpenLibrary API

---

## 🔗 Endpoints principaux

### Santé & Diagnostics
* `GET /books/health` — health check du service
* `GET /books/ping` — ping simple

### CRUD Livres
* `POST /books` — créer un livre
* `GET /books/{id}` — récupérer un livre par UUID
* `GET /books` — lister tous les livres (paginé, `?page=0&size=20`)
* `PATCH /books/{id}` — mettre à jour un livre
* `DELETE /books/{id}` — supprimer un livre

### Recherche & Synchronisation OpenLibrary
* `GET /books/search?title=...&author=...` — recherche OpenLibrary + synchronisation locale
* `POST /books/sync/{isbn}` — synchroniser un livre spécifique par ISBN
* `GET /books/by-isbn13?isbn13=...` — rechercher un livre local par ISBN-13

---

## 🛠️ Prérequis

* Java 17+
* Maven 3.8+
* Docker (optionnel)
* Accès réseau pour les appels OpenLibrary

---

## 🌐 OpenLibrary Integration

Le **book-service** intègre complètement l'API OpenLibrary pour enrichir le catalogue :

### Client OpenLibrary
* **Classe** : `com.littlebook.book.service.external.OpenLibraryClient`
* **Endpoint** : `https://openlibrary.org/search.json`
* **Paramètres supportés** : `title`, `author`, `isbn`
* **Encoding** : URLEncoder avec UTF-8 (gestion des caractères spéciaux)
* **Timeout** : 5 secondes
* **Configuration** : `openlibrary.api.base-url` dans `application.yml`

### Service de Synchronisation
* **Classe** : `com.littlebook.book.service.BookSyncService`
* Recherche sur OpenLibrary et synchronise les résultats localement
* Détecte les doublons par ISBN-13 ou OpenLibrary ID
* Enrichit les entités avec métadonnées (année, couverture, description)

### Exemple de flux
1. Client appelle `GET /books/search?title=Harry`
2. OpenLibraryClient requête `https://openlibrary.org/search.json?title=Harry`
3. BookSyncService récupère les résultats et crée les entités locales
4. Les doublons (même OpenLibrary ID) ne sont pas créés deux fois

---

## 💻 Développement — démarrer localement

1. Build :

```powershell
cd book-service
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

Le service démarre sur `http://localhost:8084`

Tests rapides :

```powershell
curl.exe -X GET "http://localhost:8084/books/ping"
curl.exe -X GET "http://localhost:8084/books?size=20"
curl.exe -X GET "http://localhost:8084/books/search?title=Harry"
curl.exe -X POST "http://localhost:8084/books/sync/9780439708180"
```

---

## 🐳 Docker

Build :

```powershell
docker build -t littlebook-book:local book-service/
```

Run via Docker Compose :

```powershell
docker-compose up -d book-service
```

Le service écoute sur le port `8084`

---

## 🗄️ Base de données

* **Type** : H2 en mémoire pour le développement
* **Configuration** : `src/main/resources/application.yml`
* **URL** : `jdbc:h2:mem:bookdb`
* **DDL** : Hibernate `ddl-auto: update` (crée les tables automatiquement)

Aucun script SQL PostgreSQL n'est appliqué. Les migrations vers PostgreSQL en production seront à gérer séparément.

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

* Ajouter authentification (JWT / Firebase / OAuth2 selon architecture globale)
* Restreindre les endpoints si nécessaire

---

## 📄 Swagger / OpenAPI

Disponible via springdoc :

```
http://localhost:8084/swagger-ui.html
```

La documentation interactive affiche tous les endpoints avec les modèles de données.

---

## 📌 Fonctionnalités implémentées

- ✅ **CRUD complet** : Create, Read, Update, Delete avec validation
- ✅ **Recherche OpenLibrary** : Par titre, auteur, ISBN avec synchronisation
- ✅ **Pagination & tri** : Support `page`, `size`, `sort` sur tous les endpoints liste
- ✅ **Gestion d'erreurs** : GlobalExceptionHandler avec réponses standardisées
- ✅ **Validation** : Jakarta Validation annotations sur les DTOs
- ✅ **H2 en mémoire** : Démarrage rapide en développement
- ✅ **RestClient Spring 6** : Client HTTP moderne et natif
- ✅ **Logging** : SLF4J avec messages structurés
- ✅ **Détection des doublons** : Vérification par ISBN-13 et OpenLibrary ID
- ✅ **URL Encoding** : Gestion correcte des caractères spéciaux (accents, espaces)

## 🔮 Perspectives futures

### Court terme (1-2 mois)
1. **JWT Authentication** — Protéger les endpoints d'écriture avec tokens
2. **Tests unitaires** — Coverage > 80% avec JUnit5 + Mockito
3. **Communication inter-services** — Review-Service appellera pour vérifier les livres
4. **Redis Caching** — Cache des recherches OpenLibrary fréquentes

### Moyen terme (3-6 mois)
1. **PostgreSQL** — Migration vers PostgreSQL en production
2. **Elasticsearch** — Indexation pour recherches ultra-rapides
3. **GraphQL API** — Alternative à REST pour requêtes complexes
4. **Rate Limiting** — Protection contre abus de l'API OpenLibrary
5. **Webhooks** — Notifications sur ajout/modification de livres

### Long terme (6-12 mois)
1. **Machine Learning** — Recommandations basées l'historique utilisateur
2. **Audit Trail** — Historique complet des modifications
3. **Data Enrichment** — Intégration Google Books, Amazon API
4. **Synchronisation temps réel** — WebSocket pour mises à jour live
5. **Multi-sources** — Support de plusieurs catalogues de livres

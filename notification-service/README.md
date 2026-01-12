# notification-service — Microservice Notifications

Ce dossier contient le microservice **notification-service**, chargé de la **gestion et diffusion des notifications utilisateurs** au sein de la plateforme LittleBook.

Il permet de **créer**, **stocker**, **consulter** et **marquer comme lues** des notifications liées aux événements métiers (reviews, nouveaux livres, recommandations, activités sociales, etc.).

Ce README couvre uniquement le périmètre du microservice `notification-service`.

---

## 🔔 Rôle du microservice

Le service fournit :

- Création de notifications utilisateurs (events métiers)
- Consultation des notifications par utilisateur
- Gestion de l’état **lu / non lu**
- Historique des notifications
- API REST exposée au front-end et aux autres microservices

---

## 🏗️ Structure du projet
src/main/java/com/littlebook/notification
├── NotificationApplication.java → Entrypoint Spring Boot
├── config/
│ ├── OpenApiConfig.java → Configuration Swagger/OpenAPI
│ ├── RestClientConfig.java → Client HTTP inter-services
│ └── SecurityConfig.java → Configuration sécurité (dev)
├── controller/
│ └── NotificationController.java → Endpoints REST
├── service/
│ └── NotificationService.java → Logique métier
├── dto/
│ ├── NotificationRequest.java → DTO création
│ └── NotificationResponse.java → DTO réponse
├── entity/
│ └── NotificationEntity.java → Entité JPA
├── repository/
│ └── NotificationRepository.java → Accès DB
src/resources
├── application.yml --> config
├── import.sql
├── schema,sql

Dockerfile                               → Build Docker


---

## 🚀 Principales caractéristiques

- **Entrypoint** : `com.littlebook.notification.NotificationApplication`
- **Port par défaut** : `8087`
- **API** : `/notifications/*`
- **DB** : H2 en mémoire (dev)
- **Communication inter-services** prête (RestClient)
- **Swagger activé**

---

## 🔗 Endpoints principaux

### Santé & Diagnostics
- `GET /notifications/health` — health check
- `GET /notifications/ping` — ping simple

### Notifications
- `POST /notifications` — créer une notification
- `GET /notifications` — lister toutes les notifications (paginé)
- `GET /notifications/user/{userId}` — notifications d’un utilisateur
- `GET /notifications/{id}` — récupérer une notification
- `PATCH /notifications/{id}/read` — marquer comme lue
- `DELETE /notifications/{id}` — supprimer une notification

---

## 🛠️ Prérequis

- Java 17+
- Maven 3.8+
- Docker (optionnel)

---

## 💻 Développement — démarrer localement

### Build

```bash
cd notification-service
mvn -DskipTests clean package
. Run via Maven :

```powershell
mvn spring-boot:run
```

3. Ou exécuter le jar :

```powershell
java -jar target/littlebook-back-0.0.1-SNAPSHOT.jar
```

Le service démarre sur `http://localhost:8087`

# 🐳 Docker

Build :

```powershell
docker build -t littlebook-notification:local notification-service/
```

Run via Docker Compose :

```powershell
docker-compose up -d notification-service
```

Le service écoute sur le port `8087`

---
## 🗄️ Base de données

* **Type** : H2 en mémoire pour le développement
* **Configuration** : `src/main/resources/application.yml`
* **URL** : `jdbc:h2:mem:notificationdb`
* **DDL** : Hibernate `ddl-auto: update` (crée les tables automatiquement)

Aucun script SQL PostgreSQL n'est appliqué. Les migrations vers PostgreSQL en production seront à gérer séparément.

## 📄 Swagger / OpenAPI

Le microservice **notification-service** expose une documentation OpenAPI interactive
générée automatiquement via **springdoc-openapi**.

### Accès à Swagger UI

Une fois le service démarré, la documentation est disponible à l’adresse suivante : http://localhost:8087/swagger-ui.html


### Contenu de la documentation

La Swagger UI permet de :

* Visualiser l’ensemble des endpoints REST du service
* Tester les requêtes directement depuis le navigateur
* Consulter les schémas de données :
  * `NotificationRequest`
  * `NotificationResponse`
* Vérifier les codes de réponse HTTP et les messages d’erreur
* Comprendre les paramètres (path, query, body)

### Endpoints documentés

* `GET /notifications` — lister les notifications
* `GET /notifications/{id}` — récupérer une notification
* `POST /notifications` — créer une notification
* `PATCH /notifications/{id}/read` — marquer une notification comme lue
* `DELETE /notifications/{id}` — supprimer une notification
* `GET /notifications/health` — health check du service
  
### Bonnes pratiques

* Toujours vérifier la Swagger UI après ajout ou modification d’un endpoint
* Maintenir les DTOs correctement annotés pour une documentation claire
* Utiliser Swagger comme référence pour l’intégration frontend et inter-services
---




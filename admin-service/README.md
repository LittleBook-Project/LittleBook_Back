# LittleBook_Back
# 📘 Backend – Spring Boot 3 + Java 17 + SQL + Firebase

LittleBook est une application de type réseau social visant à permettre aux utilisateurs de partager du contenu, de suivre d'autres membres et d'interagir à travers des publications et commentaires.  
Ce dépôt correspond à la partie **back-end**, développée avec **Spring Boot**, assurant la gestion des utilisateurs, des rôles et des futures entités (posts, relations, etc.). Ce back sera en communication avec une partie **front-end** réalisé en parallèle avec **React**

---
## 👥 Équipe de développement

- **[AlyneLDC](https://github.com/alyneldc)** — Gestion de projet / Responsable backend / documentation
- **[MathBruu](https://github.com/mathbruu)** — Responsable frontend / intégration / documentation
- **[MouniaT](https://github.com/MOUNIAT-1002)** — Responsable backend / conception / intégration / documentation
- **[ThomasKsk](https://github.com/ThomasKsk)** — Base de données / documentation

---
## 📂 Sommaire

Ce dépôt contient :
- Le **code source du back-end en Java - Spring Boot**
- La **configuration de la base de données PostgreSQL (Supabase)**
- Les **scripts d’initialisation**
- Le **Dockerfile** pour le déploiement de l’API (en attente)

L’objectif de ce dépôt est d’offrir une base solide et évolutive avant le passage vers une **architecture orientée services (AOS)**.

---
## �️ Admin service (microservice)

Ce dépôt contient désormais une structure permettant d'extraire un microservice dédié **admin**. Les points clés :

- Entrypoint : `com.littlebook.admin.AdminApplication` (scan limité au package `com.littlebook.admin`).
- Port par défaut : `8081` (fichier `src/main/resources/application.yml`).
- Endpoints d'exemple : `/admin/health`, `/admin/ping`.
- Le code existant (controllers pour Book/Review/Subscription/User) reste présent pour archivage mais n'est plus scanné par l'application admin (pour éviter les conflits lors du démarrage).

Pour démarrer uniquement le microservice admin en local :

```bash
 # admin-service — microservice Admin

 Ce dossier contient le microservice "admin" extrait du backend LittleBook.
 Il s'agit d'un petit service Spring Boot destiné à exposer des opérations d'administration (monitoring, gestion des utilisateurs/rôles, audits).

 Principales caractéristiques
 - Entrypoint : `com.littlebook.admin.AdminApplication`
 - Port par défaut : 8081
 - Endpoints d'exemple : `/admin/health`, `/admin/ping`

 Important : ce README couvre uniquement le microservice `admin-service`. Les sources plus larges du backend (monolithe) ont été archivées ou déplacées — voir `archived/` si présent.

 ## Structure du projet

 - `src/main/java/com/littlebook/admin` : code spécifique au microservice (controller, service, repository, config, dto)
 - `src/main/resources/application.yml` : configuration locale (H2 par défaut)
 - `Dockerfile` : build/run en image
 - `scripts/` : scripts d'aide (archive, cleanup)

 ## Prérequis

 - Java 17+ (ou Java 21 présent sur la machine de build)
 - Maven 3.8+
 - Docker (optionnel)

 ## Variables d'environnement utiles

 - `FIREBASE_PROJECT_ID` — identifiant Firebase (si utilisé)
 - `FIREBASE_CREDENTIALS` — chemin absolu vers la clé de service Firebase (JSON)
 - Pour override de la DB en production (exemple) :
   - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`

 Ne placez jamais de mots de passe en clair dans le repo. Utilisez des variables d'environnement ou un coffre (Vault / GitHub Secrets).

 ## Développement — lancer localement

 1) Compiler le projet (rapide, tests sautés) :

 ```bash
 cd /Users/justinekosinski/Desktop/ledoux/LittleBook_Back/admin-service
 mvn -DskipTests clean package
 ```

 2) Lancer avec Maven (utilise l'entrypoint admin) :

 ```bash
 mvn -DskipTests -Dspring-boot.run.main-class=com.littlebook.admin.AdminApplication spring-boot:run
 ```

 ou lancer le jar produit :

 ```bash
 java -jar target/littlebook-back-0.0.1-SNAPSHOT.jar
 ```

 Test rapide des endpoints :

 ```bash
 curl -sS http://localhost:8081/admin/health
 curl -sS http://localhost:8081/admin/ping
 ```

Endpoints d'administration / statistiques (exemples) :

```bash
# Liste des stats utilisateur (stockées localement dans admin DB)
curl -sS http://localhost:8081/api/stats/users

# Liste enrichie : jointure dynamique avec user-service (email, name, roles...)
curl -sS http://localhost:8081/api/stats/users/enriched

# Evénements de login
curl -sS http://localhost:8081/api/stats/login-events

# Résumé global
curl -sS http://localhost:8081/api/stats/summary
```

 Si vous utilisez un port autre que 8081, passez l'argument `--server.port=XXXX` à la JVM ou à `spring-boot:run`.

 ## Docker

 Build et run :

 ```bash
 # depuis le dossier admin-service
 docker build -t littlebook-admin:local .
 docker run -p 8081:8081 littlebook-admin:local
 ```

 ## Base de données et scripts SQL

 Par défaut, `application.yml` configure une base H2 en mémoire pour le développement local.
 Le projet contient des scripts SQL orientés PostgreSQL (ex. `schema.sql`) — ces scripts ne doivent pas être exécutés automatiquement contre H2. Le démarrage local est configuré pour ignorer l'init SQL.

 En production, activez un profil (ex. `prod`) avec une datasource PostgreSQL et utilisez Flyway ou Liquibase pour l'initialisation/les migrations.

 ## Tests

 Exécuter les tests :

 ```bash
 mvn test
 ```

 Ajoutez des tests unitaires et d'intégration dans `src/test` pour couvrir le contrôleur admin et la logique métier.

 ## Sécurité

 Actuellement la configuration de sécurité est minimale (dev). Avant production, mettez en place :

 - authentification (JWT / Firebase / OAuth2)
 - configuration des rôles et permissions pour les endpoints d'administration

 ## Bonnes pratiques & next steps

 - Ne laissez aucune credential sensible commitée. Migrer les secrets vers un vault.
 - Déplacer la gestion des schémas DB vers Flyway/Liquibase et activer par profil.
 - Ajouter une pipeline CI qui build, teste et publie l'image Docker.
 - Ajouter des metrics/opentelemetry et des endpoints d'audit pour l'administration.

## Swagger / OpenAPI

L'UI Swagger est exposée via springdoc. Après démarrage du service, ouvrez :

```
http://localhost:8081/swagger-ui.html
```

Vous y verrez la documentation interactive des endpoints exposés (ex: `/api/stats/*`).

 ## Contact / Contributeurs

 Voir la racine du monorepo pour la liste complète des contributeurs.

 ---
 Petit résumé : ce README vous permet de démarrer rapidement le microservice admin en local, de comprendre la configuration par défaut et les étapes à suivre pour rendre le service prêt pour la production.

# LittleBook Back

Backend microservices de LittleBook (gateway, book-service, review-service, auth-service, recommendation-service, ...).

## Pré-requis
- Java 17
- Maven
- Docker & Docker Compose (optionnel)

## Clé de service Firebase (important)
Récupérez la clé de service JSON depuis la console Firebase (Project Settings → Service accounts → Generate new private key).
Placez ce fichier à la racine du projet `LittleBook_Back` avec le nom exact `littlebook.json`.

Important : `littlebook.json` est ignoré par Git (ne pas le committer). Docker et les services backend l'utilisent au démarrage.

## Démarrage (avec Docker)
PowerShell :

```powershell
cd LittleBook_Back
docker compose build
docker compose up
```

Pour démarrer en arrière-plan :

```powershell
docker compose up -d
```

Arrêter :

```powershell
docker compose down
```

## Lancer sans Docker (développement local)
1. Placez `littlebook.json` à la racine de `LittleBook_Back`.
2. Construire les modules :

```powershell
cd LittleBook_Back
mvn -T 1C -DskipTests package
```

3. Lancer un service (ex. `review-service`) :

```powershell
cd review-service
mvn spring-boot:run
# ou après le package : java -jar target/*.jar
```

## Setup développeur (détails)

1. Cloner le dépôt et activer les hooks Git (une seule fois) :

```powershell
git clone <url-du-repo>
cd LittleBook_Back
.\setup-hooks.ps1    # sur Windows
# ou './setup-hooks.sh' sur macOS / Linux
```

2. Placer votre clé Firebase locale et définir les variables d'environnement :

- Windows (PowerShell) :

```powershell
$env:FIREBASE_PROJECT_ID = "littlebook-b2d2d"
$env:FIREBASE_CREDENTIALS = "C:\chemin\vers\littlebook.json"
```

- macOS / Linux :

```bash
export FIREBASE_PROJECT_ID=littlebook-b2d2d
export FIREBASE_CREDENTIALS=/chemin/vers/littlebook.json
```

3. Compiler et lancer localement :

```powershell
cd LittleBook_Back
mvn clean install
cd review-service
mvn spring-boot:run
```

4. Import de données locales pour développement :

```powershell
# depuis la racine du repo
.\start_with_books.ps1
```

## Import de données de démo
Utilisez le dossier `dev-data/` et le script `start_with_books.ps1` pour importer des livres en local.

## Nettoyage
- Supprimez les dossiers `target/` (regénérables par Maven).
- Les fichiers `*.original` dans `target/` sont des restes d'assemblage et peuvent être supprimés.

---

## Architecture microservices (schéma ASCII)

Contexte : le projet vise une architecture microservices avec une API Gateway devant plusieurs services métiers.

```
		  +-----------------------------+
		  |        Frontend (UI)        |
		  |   (Vite + React) - 5173     |
		  +-------------+---------------+
				  |
				  | HTTP / API
				  v
		  +-------------+---------------+
		  |        API Gateway          |
		  |  (gateway service - 8080)   |
		  +------+------+------+--------+
			  |      |      |        
	 +--------------+      |      +--------------+
	 |                     |                     |
	 v                     v                     v
  +-----+-------+   +---------+---------+   +-------+-------+
  | book-service |   | review-service |   |  auth-service  |
  |  (books, OL) |   |   (reviews)    |   | (firebase auth)|
  +-----+-------+   +---------+---------+   +-------+-------+
	 |                     |                     |
	 v                     |                     v
	+-----+-------+             |             +-------+-------+
	|  Database    |            |             |   Firebase    |
	| (H2 in dev / |            |             | (Auth, SA key)|
	|  Postgres)   |            |             +---------------+
	+--------------+            |
				  |
				  v
			  (autres services...)
```

Notes:
- `gateway` reçoit les requêtes frontend et route vers les services appropriés.
- Chaque service possède son propre package `service-name/src` et peut être démarré indépendamment.
- En dev, H2 (in-memory or file) est utilisé pour faciliter le développement ; en production on utilise Postgres/Supabase. Le dossier `dev-data/` permet d'initialiser des jeux de données pour le développement.


## CI / Pipelines

Le projet utilise des pipelines CI pour automatiser la compilation, les tests, la création d'artefacts et le déploiement. Exemples de comportements attendus :

- **GitHub Actions** (exemple standard) :
	1. `on: push` / `pull_request` sur `main` ou `dev`.
	2. `checkout` du code, configuration de Java/Maven.
	3. `mvn -T 1C verify` pour compiler et exécuter les tests.
	4. Construire les images Docker (ou utiliser `spring-boot:repackage`) et pousser sur un registry (GHCR ou autre).
	5. Publier les artifacts (jars, rapports) en sortie et déclencher un déploiement (Render/production) si besoin.





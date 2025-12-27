# 📘 LittleBook Backend

Backend de LittleBook : architecture microservices avec Spring Boot 3 + Java 17 pour une plateforme de partage de livres.
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
## 🚀 Stack technique

- [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) – LTS stable
- [Spring Boot 3.x](https://spring.io/projects/spring-boot) – framework backend
- [Lombok](https://projectlombok.org/) – simplification du code (getters, setters, constructeurs)
- [JUnit 5](https://junit.org/junit5/) – tests unitaires et d’intégration
- [Supabase](https://supabase.com) – base de données relationnelle (PostgreSQL)
- [Firebase](https://firebase.google.com/) – services cloud (authentification, notifications, storage…)
- [Open library](https://openlibrary.org/developers/api) - Récupération de l'ensemble des livres, genre, ...

---
## 📦 Installation

### 1. Cloner le projet
```bash
git clone https://github.com/MOUNIAT-1002/LittleBook_Back.git
cd LittleBook_Back
```

### 2. Activer les Git hooks (protection secrets)

**Une seule fois après le clone :**

```bash
# Sur Linux/Mac
./setup-hooks.sh

# Sur Windows (PowerShell)
.\setup-hooks.ps1
```
---
### 3. Setup développeur (Authentification Google via Firebase)

1. Demander la **clé de service** (fichier JSON) dans le coffre-fort d’équipe.
2. Placer le fichier en local, hors dépôt, p.ex.:
   - macOS/Linux: `~/littlebook/secrets/firebase-adminsdk.json`
   - Windows: `C:\Users\<you>\littlebook\secrets\firebase-adminsdk.json`
3. Exporter les variables d’environnement:
   - macOS/Linux:
     ```bash
     export FIREBASE_PROJECT_ID=littlebook-b2d2d
     export FIREBASE_CREDENTIALS=/ABSOLU/vers/secrets/firebase-sa.json
     ```
   - Windows (PowerShell):
     ```powershell
     $env:FIREBASE_PROJECT_ID = "littlebook-b2d2d"
     $env:FIREBASE_CREDENTIALS = "C:\<chemin>\secrets\firebase-sa.json"
     ```

## ⚙️ Compilation et execution 
### 1. Compilation
```bash
mvn clean install
```
### 2. Execution
```bash
mvn spring-boot:run
```
## 🚀 Démarrage rapide avec Docker Compose

Pour lancer les microservices principaux (admin, auth, user) sans configuration complexe:

1) Placez votre fichier de credentials Firebase à la racine du dépôt et renommez-le `littlebook.json`.
  - Ce fichier est ignoré par Git (déjà ajouté dans `.gitignore`).
  - Il sera automatiquement copié dans l'image Docker d'`auth-service` et référencé via la variable `FIREBASE_CREDENTIALS=/app/littlebook.json`.

2) Lancez Docker Compose depuis la racine du dépôt:

```powershell
# Windows PowerShell
```

3) Accédez aux Swagger UI:

Notes:

## 🐳 Démarrage avec Docker Compose

### Démarrage simple par service (racine du projet)

Depuis la racine de `LittleBook_Back/` (où se trouve le `docker-compose.yml`) pour lancer les services principaux :

```bash
docker compose up -d
```

Arrêter les services :

```bash
docker compose down
```

Rebuild d'un service après modification :

```bash
docker compose build book-service
docker compose up -d book-service
```

---
## 🧩 Implémentation actuelle

L’architecture actuelle suit une approche **monolithique** : toutes les fonctionnalités (utilisateurs, relations, posts, etc.) sont regroupées au sein d’une même API Spring Boot.

Cette approche permet un développement rapide et une meilleure cohérence initiale.  
À terme, l’objectif est de **découper l’application en microservices**, suivant une **architecture orientée services (AOS)** :
- Un service **User**
- Un service **Review**
- Un service **Suscriber / Suscribed**
- Un service **Notification**

---
## 🔮 Perspectives d’avenir

- Mise en place d’une **API Gateway** après le découpage complet en microservices  
- Création d’un **module de représentation graphique** basé sur les données utilisateurs  
- Ajout d’un **système d’envoi d’emails de notification** pour informer les utilisateurs de leurs réalisations  
- **Déploiement complet avec Docker** et orchestration des services  
- **Ouverture du projet en open-source** pour favoriser la contribution communautaire  
- Mise en place d’une **authentification avancée** (JWT / OAuth2) 

---
## 🧩 Modèle de donnée
![Modèle de donnée de l'application](images/model_donnees/md_v1.png)

---
## 🌐 Déploiement

### Déploiement en développement (local)

Pour lancer les microservices en local avec Docker Compose, voir la section "Démarrage rapide avec Docker Compose" ci-dessus.

### Déploiement en production

Le projet utilise une approche **CI/CD automatisée** avec GitHub Actions.

#### Pipeline CI/CD

À chaque push sur les branches `main` :
1. **Build automatique** de chaque microservice
2. **Push des images Docker** vers GitHub Container Registry (`ghcr.io`)
3. **Tag automatique** avec le SHA du commit et `latest` pour la branche principale

#### Déploiement gratuit sur Render.com (Recommandé)

**Render.com** offre un plan gratuit parfait pour les projets comme LittleBook.

1. **Créez un compte sur [Render.com](https://render.com)**

2. **Connectez votre dépôt GitHub** :
   - Dashboard → New → Blueprint
   - Sélectionnez le dépôt `LittleBook_Back`
   - Render détectera automatiquement le fichier `render.yaml`

3. **Configurez les secrets** (uniquement pour auth-service) :
   - Dans le dashboard Render, allez sur le service `littlebook-auth`
   - Environment → Add Secret File
   - Nom : `FIREBASE_CREDENTIALS`
   - Contenu : Collez le contenu de votre fichier `littlebook.json`

4. **Déployez** :
   - Cliquez sur "Apply" pour déployer tous les services
   - Render build et déploie automatiquement chaque microservice
   - Vous obtiendrez une URL publique pour chaque service

5. **Accédez à vos services** :
   - `https://littlebook-admin.onrender.com`
   - `https://littlebook-auth.onrender.com`
   - `https://littlebook-user.onrender.com`
   - `https://littlebook-review.onrender.com`
   - `https://littlebook-book.onrender.com`

⚠️ **Note** : Le plan gratuit met les services en veille après 15 min d'inactivité (démarrage ~30s).

#### Déployer sur un serveur VPS

1. **Sur votre serveur de production**, installez Docker et Docker Compose

2. **Authentifiez-vous à GitHub Container Registry** :
   ```bash
   echo $GITHUB_TOKEN | docker login ghcr.io -u VOTRE_USERNAME --password-stdin
   ```

3. **Clonez le dépôt** :
   ```bash
   git clone https://github.com/LittleBook-Project/LittleBook_Back.git
   cd LittleBook_Back
   ```

4. **Configurez les variables d'environnement** :
   ```bash
   cp .env.prod.example .env.prod
   # Éditez .env.prod avec vos valeurs
   ```

5. **Placez votre fichier de credentials Firebase** `littlebook.json` à la racine

6. **Lancez les services** :
   ```bash
   docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
   ```

7. **Vérifiez le statut** :
   ```bash
   docker-compose -f docker-compose.prod.yml ps
   ```

#### Autres plateformes de déploiement

- **Render.com** : ✅ Gratuit, simple, recommandé (voir ci-dessus)
- **Railway.app** : Gratuit avec 500h/mois, détection auto Dockerfile
- **Fly.io** : Gratuit jusqu'à 3 VMs, bon pour microservices
- **VPS** : DigitalOcean, OVH, AWS EC2 (payant mais plus de contrôle)

#### Notes de sécurité

- ⚠️ En production, configurez un **reverse proxy** (Nginx, Traefik) avec HTTPS
- ⚠️ Restreignez les **CORS origins** dans `application.yml` à votre domaine frontend
- ⚠️ Utilisez des **secrets** pour les credentials Firebase (ne pas commiter `littlebook.json`)
- ⚠️ Configurez un **firewall** pour n'exposer que les ports nécessaires

---
## 🔗 Liens

Lien du dépot github : 
👉 https://github.com/LittleBook-Project/LittleBook_Back/

Images Docker :
👉 https://github.com/orgs/LittleBook-Project/packages

Lien de production :
👉 **En attente de la fin complète du projet**

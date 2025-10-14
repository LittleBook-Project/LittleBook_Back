# LittleBook_Back

# 📘 Backend – Spring Boot 3 + Java 17 + SQL + Firebase

## 🚀 Stack technique

- [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) – LTS stable
- [Spring Boot 3.x](https://spring.io/projects/spring-boot) – framework backend
- [Lombok](https://projectlombok.org/) – simplification du code (getters, setters, constructeurs)
- [JUnit 5](https://junit.org/junit5/) – tests unitaires et d’intégration
- [SQL Database](https://www.mysql.com/) – base de données relationnelle (MySQL, PostgreSQL…)
- [Firebase](https://firebase.google.com/) – services cloud (authentification, notifications, storage…)
- [Open library](https://openlibrary.org/developers/api) - Récupération de l'ensemble des livres, genre, ...

---

## 📦 Installation

### 1. Cloner le projet

```bash
git clone https://github.com/MOUNIAT-1002/LittleBook_Back.git
```

---

## ⚙️ Compilation et execution

### 1. Compilation

```bash
mvn clean install
```

### 2. Execution

```bash
mvn spring-boot:run
```

---

## 🧩 Modèle de donnée

![Modèle de donnée de l'application](images/model_donnees/md_v1.png)

## Setup dev (Auth Google via Firebase)

1. Demande ta **clé de service** (JSON) dans le coffre-fort d’équipe.
2. Place le fichier en local, hors dépôt, p.ex.:
   - macOS/Linux: `~/littlebook/secrets/firebase-adminsdk.json`
   - Windows: `C:\Users\<you>\littlebook\secrets\firebase-adminsdk.json`
3. Exporte les variables d’environnement:
   - macOS/Linux:
     ```bash
     export FIREBASE_PROJECT_ID=littlebook-b2d2d
     export FIREBASE_CREDENTIALS=/ABSOLU/vers/secrets/firebase-sa.json
     ```
   - Windows (PowerShell):
     ```powershell
     $env:FIREBASE_PROJECT_ID = "littlebook-b2d2d"
     $env:FIREBASE_CREDENTIALS = "C:\chemin\secrets\firebase-sa.json"
     ```
4. Lance l’API:
   ```bash
   mvn spring-boot:run
   ```

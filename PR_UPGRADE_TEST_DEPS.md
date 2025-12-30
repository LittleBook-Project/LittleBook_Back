Proposition de PR: Mise à jour des dépendances de test pour compatibilité Java 25

But: résoudre des échecs d'instrumentation et des limitations de Mockito/Byte Buddy/JaCoCo sur Java 25.

Modifications proposées

- Déclare des propriétés de version dans `pom.xml` :
  - `mockito.version` = 5.11.0
  - `bytebuddy.version` = 1.15.0
  - `jacoco.version` = 0.8.13

- Force `mockito-core` à la version déclarée et ajoute explicitement `byte-buddy` et `byte-buddy-agent` en scope `test` pour s'assurer qu'une version plus récente est sur le classpath de test.
- Met à jour la configuration du plugin JaCoCo pour utiliser `${jacoco.version}`.

Pourquoi ces changements

- Les erreurs observées localement venaient de Byte Buddy / JaCoCo ne supportant pas le format de classes généré par Java 25. Mettre une version plus récente (ou explicite) réduit le risque d'Unsupported class file major version et permet à Mockito d'instrumenter correctement.

Vérifications à faire localement

1) Tester avec Java 25 (dev machine) en désactivant JaCoCo si nécessaire :

```bash
cd LittleBook_Back/auth-service
mvn -Dnet.bytebuddy.experimental=true -Djacoco.skip=true -Dtest=AuthApplicationTests,CorsPreflightTest,SecurityIntegrationTest test
```

2) Tester en mode « propre » avec Java 17 (recommandé) :

```bash
# macOS zsh
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
cd LittleBook_Back
mvn -T1C -DskipTests=false test
```

Notes & recommandations

- L'option `-Dnet.bytebuddy.experimental=true` permet une voie d'urgence pour Byte Buddy sur les JDK récents. Elle n'est pas idéale en CI/production : préférez une mise à jour des dépendances ou exécuter les tests sur un JDK officiellement supporté (ex: 17) en CI.
- Si la CI utilise Java 17, ces modifications restent sans effet négatif et permettent une meilleure compatibilité.
- Si vous voulez, je peux :
  - créer une branche `dev/upgrade-test-deps-java25` et ouvrir une PR automatique avec ces changements ;
  - ou modifier aussi les POMs de sous-modules si certains modules redéclarent des versions incompatibles.

Prochaine étape suggérée

- Confirmer que vous voulez que j'ouvre la branche et la PR ici (je peux créer la branche, appliquer ces changements et ajouter un message de PR). Si oui, je vais aussi scanner les POMs des modules pour harmoniser les versions si nécessaire.

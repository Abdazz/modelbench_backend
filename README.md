# ModelBench, backend

API REST de gestion de jeux de donnees, de modeles de Machine Learning et d'experimentations.
Devoir de Master Intelligence Artificielle, Developpement Full-Stack, 2026/2027.

## Comptes de demonstration

| Login | Mot de passe | Role | Droits |
|---|---|---|---|
| `admin@example.com` | `admin123` | ADMIN | Lecture et ecriture |
| `chercheur@example.com` | `chercheur123` | CHERCHEUR | Lecture seule |

Swagger reste accessible **sans connexion**.

## Prerequis

- Java 21
- Maven, via le wrapper `./mvnw` fourni.
- PostgreSQL 14 ou superieur, **uniquement** pour le profil par defaut

## Demarrage le plus rapide, sans aucune base a installer

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

L'application demarre sur le port 8090 avec une base H2 en memoire, remplie automatiquement de
18 datasets, 18 modeles et 24 experimentations.

## Demarrage avec PostgreSQL

Option 1, avec Docker :

```bash
docker compose up -d
./mvnw spring-boot:run
```

Option 2, avec une instance PostgreSQL locale, en creant d'abord la base :

```bash
createdb -U postgres model_bench_db
./mvnw spring-boot:run
```

Identifiants surchargeables par variables d'environnement : `DB_HOST`, `DB_PORT`, `DB_NAME`,
`DB_USER`, `DB_PASSWORD`.

## Documentation de l'API

| Ressource | URL |
|---|---|
| Swagger UI | http://localhost:8090/swagger |
| Contrat OpenAPI | http://localhost:8090/v3/api-docs |
| Console H2, profil h2 uniquement | http://localhost:8090/h2-console |

Pour essayer un endpoint protege depuis Swagger : appeler `POST /api/auth/login`, copier la valeur
de `token`, cliquer sur **Authorize** en haut a droite et la coller.

## Lancer les tests

```bash
./mvnw test
```

Les tests s'executent sur H2 sous le profil `test` : aucune base externe n'est necessaire.

## Endpoints principaux

| Methode et chemin | Role requis | Description |
|---|---|---|
| `POST /api/auth/login` | public | Obtenir un jeton |
| `GET /api/auth/moi` | authentifie | Identite du porteur du jeton |
| `GET /api/datasets` | authentifie | Lister avec pagination, tri et filtres |
| `POST /api/datasets` | ADMIN | Creer |
| `PUT /api/datasets/{id}` | ADMIN | Modifier |
| `DELETE /api/datasets/{id}` | ADMIN | Supprimer, refuse en 409 si reference |
| `GET /api/modeles`, `GET /api/experimentations` | authentifie | Idem pour les deux autres entites |
| `GET /api/reference/formats-dataset` | authentifie | Valeurs d'enumeration |
| `GET /api/statistiques/synthese` | authentifie | Indicateurs du tableau de bord |
| `GET/POST/PUT/DELETE /api/utilisateurs` | ADMIN, y compris en lecture | Gestion des comptes |

Parametres de pagination communs : `?page=0&size=10&sort=nom,asc`.

## Architecture

```
com.example.modelbench
  config/         OpenApiConfig, CorsConfig, SecurityConfig
  entity/         Dataset, ModeleML, Experimentation, Utilisateur, enums/
  repository/     interfaces Spring Data, avec JpaSpecificationExecutor
  specification/  criteres de filtrage dynamiques
  dto/            records de requete et de reponse, ApiError, PageResponse
  mapper/         conversion entite vers DTO
  service/        contrats metier, impl/ pour les realisations
  controller/     exposition HTTP et annotations OpenAPI
  exception/      exceptions metier et gestion centralisee
  bootstrap/      jeu de donnees de demonstration
```

Toutes les erreurs, y compris celles de securite, partagent le meme corps JSON :
`timestamp`, `status`, `code`, `message`, `path`, et `errors` sur les erreurs de validation.

## Note de securite

Ce projet est configure pour un usage **local**, de developpement et de demonstration. Trois choix
sont volontaires et devraient etre modifies avant tout deploiement reel :

| Element | Valeur par defaut | A faire en production |
|---|---|---|
| Cle de signature JWT | Valeur en clair dans `application.properties` | Fournir via la variable d'environnement `JWT_SECRET` |
| Identifiants PostgreSQL | `postgres` / `postgres`, dans `application-postgres.properties` et `docker-compose.yml` | Fournir via `DB_USER` et `DB_PASSWORD` |
| Console H2 | Activee sous le profil `h2` | Ne pas utiliser le profil `h2` hors developpement |
| Duree de vie des jetons | Un jeton reste valide 8 heures apres son emission, meme si le compte est desactive, retrograde ou supprime entre-temps (aucune verification en base a chaque requete) | Reduire `security.jwt.duree-validite-secondes`, ou mettre en place une liste de revocation, avant un deploiement expose a des utilisateurs non maitrises |

Les comptes de demonstration `admin@example.com` et `chercheur@example.com` sont eux aussi crees
avec des mots de passe connus, par construction : ils servent a rendre l'application immediatement
utilisable par un evaluateur.

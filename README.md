# Projet Architecture Logicielle -- Groupe 8

## Presentation

Ce projet a ete realise dans le cadre du cours d'Introduction a l'Architecture Logicielle (Master 1, Semestre 2). Il s'agit d'une API REST construite avec Spring Boot qui met en pratique les concepts vus en cours, notamment JPA, la separation des responsabilites, l'authentification par token et la gestion des roles.

L'application est un systeme d'authentification et de gestion d'utilisateurs avec un CRUD produit, le tout securise par JWT et Spring Security.

---

## Demarche et choix techniques

### Pourquoi Spring Boot

On a choisi Spring Boot parce que c'est le framework qu'on a utilise en cours. Il permet de mettre en place rapidement une API REST avec toutes les briques necessaires (web, securite, persistence). On utilise la version 4.0.2 avec Java 17.

### Base de donnees H2

On utilise H2 en mode memoire (in-memory) pour simplifier le developpement. Pas besoin d'installer un serveur de base de donnees, tout se reinitialise a chaque demarrage. C'est suffisant pour une demo et ca permet de tester rapidement. La console H2 est accessible a `/h2-console` pour verifier les donnees directement.

### JPA et les entites

Comme vu dans le cours sur JPA, on a defini nos entites avec les annotations standard :

- `@Entity`, `@Table` pour marquer les classes comme entites JPA
- `@Id`, `@GeneratedValue` pour les cles primaires auto-generees
- `@ManyToMany` pour la relation User/Role (un user peut avoir plusieurs roles, un role peut etre assigne a plusieurs users)
- `@ManyToOne` pour la relation Credential/User et AuthToken/User
- `@OneToMany` pour les relations inverses dans User
- `@JoinTable` pour definir les tables d'association

Les entites du projet sont :

| Entite | Description |
|--------|-------------|
| User | Utilisateur avec username, email, password, roles |
| Role | Role (USER, ADMIN, MODERATOR) avec relation ManyToMany vers User |
| Credential | Credentials separes de User (type PASSWORD/API_KEY, hash, actif) |
| AuthToken | Token d'authentification stocke en base avec expiration et revocation |
| Product | Produit simple avec id et nom |
| SecurityAlert | Alertes de securite (tentatives de login, etc.) |

Le choix de separer les credentials de l'utilisateur (entite Credential a part) vient du cours. L'idee c'est qu'un utilisateur peut avoir plusieurs types de credentials (mot de passe, cle API, etc.) et qu'on ne stocke pas le secret en clair dans l'entite User directement.

### Separation des responsabilites

On a organise le code en couches comme vu en cours :

```
controller/    --> Recoit les requetes HTTP, appelle les services
service/       --> Logique metier (AuthService, UserService, SecurityAlertService)
repository/    --> Acces aux donnees via JpaRepository (Spring Data)
entity/        --> Entites JPA (les objets persistes en base)
config/        --> Configuration Spring Security
security/      --> Filtre JWT, UserDetailsService
util/          --> Utilitaires (JwtUtil, HashUtil)
init/          --> Initialisation des donnees au demarrage
```

Chaque couche a son role et ne fait que ca. Les controllers ne font pas de logique metier, les services ne font pas d'acces direct a la base, etc.

### Authentification et securite

On utilise JWT (JSON Web Token) pour l'authentification. Quand un utilisateur se connecte, on lui renvoie un token qu'il doit ensuite envoyer dans le header `Authorization: Bearer <token>` pour acceder aux endpoints proteges.

Le flow est le suivant :

1. L'utilisateur envoie son username et password a POST /api/auth/login
2. Spring Security verifie les credentials via le AuthenticationManager
3. Si c'est bon, on genere un JWT avec JwtUtil et on le stocke aussi en base (entite AuthToken)
4. Le token est renvoye au client
5. A chaque requete, le JwtAuthenticationFilter intercepte la requete, extrait le token du header, le valide, et authentifie l'utilisateur dans le contexte Spring Security

On a aussi implemente le logout par revocation du token en base (comme vu en cours avec les tokens opaques).

Le hashing des mots de passe est fait avec BCrypt (via Spring Security) pour la partie authentification, et on a aussi un HashUtil (SHA-256) pour les credentials separes, comme dans le modele du cours.

### Gestion des roles

On a trois roles : USER, ADMIN et MODERATOR. Les endpoints sont proteges avec `@PreAuthorize` et la configuration de Spring Security :

- `/api/auth/**` : accessible a tous (login, register, logout)
- `/api/products/**` : accessible a tous (CRUD produits)
- `/api/admin/**` : reserve aux ADMIN
- `/api/security-alerts/**` : reserve aux ADMIN
- `/api/users/**` : reserve aux ADMIN et MODERATOR (avec exceptions pour son propre profil)

### Initialisation des donnees

Les donnees sont initialisees au demarrage via un composant `DataInitializer` annote `@Component` avec `@PostConstruct`. C'est l'approche qu'on a vue en cours (initialisation programmatique plutot que par fichier SQL). On cree automatiquement :

- Les roles (USER, ADMIN, MODERATOR)
- Un utilisateur admin (login: admin, password: adminpass)
- Un utilisateur standard (login: student1, password: password)
- Un utilisateur moderateur (login: john_doe, password: password123)
- Deux produits par defaut (Honey, Almond)

---

## Structure du projet

```
demo/
  src/main/java/com/example/demo/
    SoftwareArchitectureGroup8Application.java   -- point d'entree
    config/
      SecurityConfig.java                        -- configuration Spring Security
    controller/
      AuthController.java                        -- login, register, logout, validate
      AdminController.java                       -- gestion users/credentials (admin)
      ProductController.java                     -- CRUD produits
      UserController.java                        -- gestion profil utilisateur
      SecurityAlertController.java               -- consultation alertes securite
    entity/
      User.java                                  -- entite utilisateur
      Role.java                                  -- entite role
      Credential.java                            -- entite credential (separe de User)
      AuthToken.java                             -- entite token d'authentification
      Product.java                               -- entite produit
      SecurityAlert.java                         -- entite alerte de securite
    repository/
      UserRepository.java
      RoleRepository.java
      CredentialRepository.java
      AuthTokenRepository.java
      ProductRepository.java
      SecurityAlertRepository.java
    service/
      AuthService.java                           -- logique d'authentification
      UserService.java                           -- logique utilisateur
      SecurityAlertService.java                  -- logique alertes
    security/
      JwtAuthenticationFilter.java               -- filtre JWT
      CustomUserDetailsService.java              -- chargement users pour Spring Security
    util/
      JwtUtil.java                               -- generation/validation JWT
      HashUtil.java                              -- hashing SHA-256
    init/
      DataInitializer.java                       -- init donnees au demarrage
  src/main/resources/
    application.properties                       -- configuration
```

---

## Manuel d'utilisation

### Prerequis

- Java 17 ou superieur
- Maven (ou utiliser le wrapper mvnw inclus)

### Lancement

```bash
cd demo
./mvnw spring-boot:run
```

Ou sous Windows :

```bash
cd demo
mvnw.cmd spring-boot:run
```

L'application demarre sur le port 8080 par defaut. Si le port est occupe, modifier `server.port` dans `application.properties`.

### Console H2

Pour voir les donnees en base, ouvrir dans un navigateur :

```
http://localhost:8080/h2-console
```

- JDBC URL : `jdbc:h2:mem:authdb`
- Username : `sa`
- Password : (laisser vide)

### Utilisateurs par defaut

| Username | Password | Roles |
|----------|----------|-------|
| admin | adminpass | ADMIN, USER, MODERATOR |
| student1 | password | USER |
| john_doe | password123 | USER, MODERATOR |

---

### Endpoints et exemples curl

#### 1. Authentification

Login (recuperer un token) :

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"adminpass"}'
```

Reponse :

```json
{
  "token": "<JWT_TOKEN>",
  "type": "Bearer",
  "username": "admin"
}
```

Inscription :

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"new@example.com","password":"mypassword"}'
```

Valider un token :

```bash
curl http://localhost:8080/api/auth/validate/<TOKEN>
```

Logout (revoquer un token) :

```bash
curl -X DELETE http://localhost:8080/api/auth/logout/<TOKEN>
```

Lister les utilisateurs :

```bash
curl http://localhost:8080/api/auth/users
```

#### 2. Produits (accessible sans authentification)

Lister tous les produits :

```bash
curl http://localhost:8080/api/products
```

Recuperer un produit par id :

```bash
curl http://localhost:8080/api/products/1
```

Creer un produit :

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Walnut"}'
```

Modifier un produit :

```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Organic Honey"}'
```

Supprimer un produit :

```bash
curl -X DELETE http://localhost:8080/api/products/1
```

#### 3. Administration (necessite un token ADMIN)

D'abord recuperer le token admin :

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"adminpass"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
```

Creer un utilisateur :

```bash
curl -X POST http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"newstudent","email":"student@example.com"}'
```

Ajouter un credential a un utilisateur :

```bash
curl -X POST http://localhost:8080/api/admin/users/newstudent/credentials \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"PASSWORD","secret":"studentpass"}'
```

Voir les credentials d'un utilisateur :

```bash
curl http://localhost:8080/api/admin/users/newstudent/credentials \
  -H "Authorization: Bearer $TOKEN"
```

Supprimer un utilisateur :

```bash
curl -X DELETE http://localhost:8080/api/admin/users/newstudent \
  -H "Authorization: Bearer $TOKEN"
```

#### 4. Gestion des utilisateurs (ADMIN ou MODERATOR)

Lister tous les utilisateurs :

```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN"
```

Voir un utilisateur par id :

```bash
curl http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### 5. Alertes de securite (ADMIN uniquement)

Voir toutes les alertes :

```bash
curl http://localhost:8080/api/security-alerts \
  -H "Authorization: Bearer $TOKEN"
```

Filtrer par severite :

```bash
curl http://localhost:8080/api/security-alerts/severity/MEDIUM \
  -H "Authorization: Bearer $TOKEN"
```

---

### Tableau recapitulatif des routes

| Methode | URL | Auth requise | Role | Description |
|---------|-----|-------------|------|-------------|
| POST | /api/auth/login | Non | - | Login, renvoie un token |
| POST | /api/auth/register | Non | - | Inscription |
| GET | /api/auth/validate/{token} | Non | - | Verifier si un token est valide |
| POST | /api/auth/validate | Non | - | Verifier un token (body JSON) |
| DELETE | /api/auth/logout/{token} | Non | - | Revoquer un token |
| GET | /api/auth/users | Non | - | Lister les utilisateurs |
| GET | /api/products | Non | - | Lister les produits |
| GET | /api/products/{id} | Non | - | Un produit par id |
| POST | /api/products | Non | - | Creer un produit |
| PUT | /api/products/{id} | Non | - | Modifier un produit |
| DELETE | /api/products/{id} | Non | - | Supprimer un produit |
| POST | /api/admin/users | Oui | ADMIN | Creer un utilisateur |
| POST | /api/admin/users/{u}/credentials | Oui | ADMIN | Ajouter un credential |
| GET | /api/admin/users/{u}/credentials | Oui | ADMIN | Voir les credentials |
| DELETE | /api/admin/users/{u} | Oui | ADMIN | Supprimer un utilisateur |
| GET | /api/users | Oui | ADMIN, MODERATOR | Lister les utilisateurs |
| GET | /api/users/{id} | Oui | ADMIN, MODERATOR | Voir un utilisateur |
| PUT | /api/users/{id} | Oui | ADMIN ou soi-meme | Modifier un utilisateur |
| DELETE | /api/users/{id} | Oui | ADMIN | Supprimer un utilisateur |
| GET | /api/security-alerts | Oui | ADMIN | Voir les alertes |
| GET | /api/security-alerts/severity/{s} | Oui | ADMIN | Filtrer alertes par severite |
| GET | /api/security-alerts/type/{t} | Oui | ADMIN | Filtrer alertes par type |

---

## Ce qui vient du cours

- Les entites JPA avec annotations (`@Entity`, `@Table`, `@ManyToMany`, `@ManyToOne`, `@OneToMany`, `@JoinTable`)
- Les repositories Spring Data (`JpaRepository`)
- La separation User / Credential (le credential est une entite a part, pas un champ dans User)
- L'entite AuthToken pour stocker et revoquer les tokens
- L'initialisation avec `@PostConstruct` dans un `@Component`
- Le hashing avec SHA-256 (HashUtil)
- L'architecture en couches (controller / service / repository / entity)
- Les REST controllers avec les annotations `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, etc.
- La gestion des roles et authorities
- Le CRUD produit complet

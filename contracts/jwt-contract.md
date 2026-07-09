# JWT Contract (FROZEN)

Every service validates JWTs **locally** using a shared symmetric signing key. Only
user-auth-service **issues** tokens; all other services only **validate** them. No network
hop is required for authentication.

## Signing

- Algorithm: **HS512** (`io.jsonwebtoken.SignatureAlgorithm.HS512`).
- Key: symmetric secret, provided via config property `jwt.secret`, used as
  `new SecretKeySpec(secret.getBytes(), "HmacSHA512")`.
- Shared secret (dev/default, identical to the monolith `application.properties`):

  ```
  jwt.secret=nRvyYC4soFxBdZ-F-5Nnzz5USXstR1YylsTd-mA0aKtI9HUlriGrtkf-TiuDapkLiUCogO3JOK7kwZisrHp6wA
  jwt.sessionTime=86400
  ```

  In production every service reads the SAME `jwt.secret` from its environment. Rotating the
  key requires rotating it in all services simultaneously.

## Claims

- `sub` = user **id** (the `users.id` UUID string). This is the ONLY claim relied upon.
- `exp` = now + `jwt.sessionTime` seconds (default 86400 = 24h).
- No roles/authorities claim; authorization is resource-ownership based (see below).

## Token lifecycle

- **Issue** (user-auth-service only): `Jwts.builder().setSubject(user.id).setExpiration(exp).signWith(key).compact()`.
- **Validate** (all services): parse with the signing key; on success extract `sub`; on any
  exception treat request as anonymous (no principal). This mirrors
  `DefaultJwtService.getSubFromToken` returning `Optional.empty()` on failure.

## Header & propagation

- Public clients send `Authorization: Bearer <jwt>`.
- The gateway and every service forward the **same** `Authorization` header on internal
  service-to-service calls, so downstream services can resolve the current user identically.
- Endpoints that are public in the monolith remain public (see per-service OpenAPI
  `security` blocks). Notably `GET /articles/**`, `GET /profiles/**`, `GET /tags`,
  `POST /users`, `POST /users/login` are anonymous-allowed; `GET /articles/feed` and all
  writes require a valid token.

## Authorization rules (moved to user-auth-service contract, enforced by owning service)

Reproduce `AuthorizationService` semantics:

- `canWriteArticle(user, article)` = `user.id == article.userId`. Enforced by article-service.
- `canWriteComment(user, article, comment)` = `user.id == article.userId || user.id == comment.userId`.
  Enforced by comment-service, which obtains `article.userId` via the article-service contract
  (`GET /internal/articles/{slug}` returns `authorId`) and `comment.userId` from its own store.

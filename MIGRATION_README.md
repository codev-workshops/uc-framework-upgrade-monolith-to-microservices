# Monolith → Microservices Migration

This document describes the decomposition of the RealWorld/Conduit Spring Boot monolith into
independently-deployable microservices, the new data ownership model, and the deployment
order. The frozen API/data contracts live in [`contracts/`](contracts/README.md).

## Target architecture

```
                         ┌─────────────────────────┐
        public REST +    │      api-gateway / BFF   │   restores original public REST paths
        GraphQL clients ─▶│  (routing + GraphQL DGS) │   + DGS GraphQL, forwards JWT
                         └───────────┬─────────────┘
             ┌──────────────┬────────┼───────────┬───────────────┐
             ▼              ▼        ▼           ▼               ▼
     user-auth-service  profile-  article-   favorite-      comment-
       (users)          service   service    service        service
                        (follows) (articles, (article_      (comments)
                                   tags,      favorites)
                                   article_tags)
```

Each service has its **own database** (own Flyway migration set, own datasource). There are
**no cross-service SQL joins and no cross-service foreign keys** — identifiers that used to be
FKs (`articles.user_id`, `comments.user_id`, `comments.article_id`, `article_favorites.*`,
`follows.*`) are now opaque references resolved over HTTP.

## Data ownership

| Table | Owner | Former joins now resolved by |
|---|---|---|
| `users` | user-auth-service | — |
| `follows` | profile-service | user identity via user-auth `GET /internal/users/{id}` |
| `articles`, `tags`, `article_tags` | article-service | author via user-auth; counts/favorited via favorite-service; following via profile-service |
| `article_favorites` | favorite-service | — (stores article id + user id only) |
| `comments` | comment-service | article (slug→id, authorId) via article-service; follow flags via profile-service; author via user-auth |

## Composition strategy

**Synchronous HTTP fan-out** (chosen; not event-driven). article-service is the article
read-model composer. Full rationale + call flows in
[`contracts/README.md`](contracts/README.md#composition-strategy-chosen-synchronous-fan-out).

## Cross-domain couplings resolved (from Phase 0 analysis)

- `ArticleReadService.xml` joined `articles + article_tags + tags + users + article_favorites`.
  → article-service keeps `articles/tags/article_tags` locally; `users` join replaced by
  user-auth calls; `article_favorites` join (for the `favorited=` filter and counts) replaced
  by favorite-service calls (`/internal/favorites/by-user`, `/internal/favorites/counts`,
  `/internal/favorites/status`).
- `ArticleQueryService.fillExtraInfo / setFavoriteCount / setIsFavorite / setIsFollowingAuthor`
  → article-service composition step calling favorite-service + profile-service.
- `findUserFeed` needs the follow graph → profile-service `/internal/follows/followed`.
- `CommentsApi` + `ArticleFavoriteApi` resolve an Article by slug before writing →
  article-service `/internal/articles/{slug}` returns `{id, slug, authorId}`.
- `CommentQueryService` enriches comment authors with follow flags →
  profile-service `/internal/follows/among`.
- `AuthorizationService.canWriteComment` needs article author + comment author →
  comment-service fetches `authorId` from article-service, `comment.userId` locally.
- `CommentReadService.xml` joined `comments + users` → author projection via user-auth.
- `UserRelationshipQueryService.xml` (`follows` queries) → profile-service internal endpoints.

## Extraction / deployment order

Bring services up in dependency order (also the recommended extraction order):

```
1. user-auth-service   (no outbound domain deps)
2. profile-service     (needs user identity)
3. article-service     (needs user-auth, favorite, profile)
4. favorite-service    (stores article/user ids; recomposition done by article/gateway)
5. comment-service     (needs article, profile, user-auth)
6. api-gateway         (needs all of the above)
```

At runtime the only near-cycle is article ↔ favorite: article-service **calls** favorite-service
for counts/status, favorite-service does **not** call article-service, so there is no hard
startup cycle. Gateway-level slug→id resolution for favorite/comment mutations keeps favorite
and comment services free of a direct dependency on article for writes where possible.

## Local run

`docker-compose.yml` (repo root) wires every service + its database + the gateway. The gateway
exposes the original public API on port 8080, identical to the monolith.

```
docker compose up --build          # all services + gateway; API on http://localhost:8080
```

Without Docker (JDK 11 required):

```
for s in services/*/; do (cd "$s" && ./gradlew bootJar -x test); done
./scripts/run-local.sh             # starts all 6 processes wired over localhost
```

## api-gateway / BFF

`services/api-gateway/` restores the monolith's public surface on port 8080:

- **REST** — `RestGatewayController` transparently reverse-proxies each public path to the owning
  service (method, query, body and the `Authorization` header are preserved). The favorite
  endpoints are special-cased in `FavoriteGatewayController`: favorite-service returns favorite
  *state* only, so the gateway resolves `slug→id` (article-service `/internal/articles/{slug}`),
  mutates favorite state (favorite-service), then asks article-service
  (`/internal/articles/{id}/data`) to recompose the full `ArticleData` — reproducing the
  monolith's `{ "article": { ... } }` response.
- **GraphQL** — the original `schema.graphqls` is served by DGS in the gateway. Datafetchers
  (`io.spring.gateway.graphql.datafetchers.*`) fan out to the services via `GatewayClient`,
  forwarding the request's `Authorization` header through a request-scoped `RequestAuthContext`.
  Composed `Article`/`Comment`/`Profile`/`User` types are built from the services' already-composed
  JSON.

### GraphQL parity notes / remaining cross-boundary items

- The monolith's GraphQL connections use **date-based cursors** (`ArticleQueryService
  .findRecentArticlesWithCursor`, `CommentQueryService.findByArticleIdWithCursor`). The public
  REST list endpoints the gateway consumes are **offset/limit** based, so gateway connection
  cursors are **offset indices** (`first`/`after` fully supported; `last`/`before` are best-effort).
  Making GraphQL cursor-identical would require exposing the cursor queries on the internal REST
  contracts (a contract change across article- and comment-service).
- `Comment.article` (the back-reference from a comment to its article in the GraphQL schema) is
  not resolved by the gateway: the public comment payload intentionally hides `articleId`
  (`@JsonIgnore`), so the gateway cannot cheaply resolve it without a new internal endpoint. It is
  unused by the RealWorld frontend (which is REST). Documented here as a known gap.
- `Profile.feed` is resolved as the *authenticated current user's* feed (the public API only
  exposes a feed for the current user), matching how the frontend uses it.

## Verification (behaviour parity with the monolith)

Both suites were run against the live decomposed stack (all 6 services on localhost):

- `scripts/e2e-rest-parity.sh` — register/login, current user, tags, article CRUD, favorite/
  unfavorite recomposition (`favorited`/`favoritesCount`), comment CRUD, profile follow/unfollow,
  feed. **18/18 pass.**
- `scripts/e2e-graphql.sh` — `login`, `me`, `tags`, `article`, `articles` connection,
  `favoriteArticle`/`unfavoriteArticle`, `addComment`/`deleteComment`, `article.comments`,
  `profile`, `followUser`/`unfollowUser`, `createUser` (UserResult union), `updateUser`,
  `deleteArticle`. **18/18 pass.**

The gateway module additionally ships CI-safe WireMock integration tests
(`GatewayIntegrationTest`) covering routing, favorite recomposition and GraphQL composition.

The existing Selenium smoke suite (`src/test/resources/selenium/`) targets `api.url=http://
localhost:8080`; since the gateway restores that exact API, the suite runs against the gateway
unchanged (config-only) — no monolith source was modified.

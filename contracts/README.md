# Microservices Contracts (FROZEN)

This directory is the **frozen contract** for decomposing the RealWorld/Conduit monolith
into microservices. Child sessions implementing each service MUST build against these
contracts and MUST NOT change the external payload shapes. Any contract change requires a
new orchestrator decision and re-freeze.

## Services & data ownership

| Service | Owns tables | Responsibility |
|---|---|---|
| **user-auth-service** | `users` | Registration, login, current-user, user lookup, JWT issuance + validation contract, `AuthorizationService`, `PasswordEncoder`. No outbound domain deps. |
| **profile-service** | `follows` | Follow/unfollow, "does A follow B", "list users A follows", "which of these authors does A follow". Resolves user identity via user-auth contract. |
| **article-service** | `articles`, `tags`, `article_tags` | Article + Tag CRUD, slug resolution, article read-model **composition** (see below). Keeps the multi-table article-save transaction intact. |
| **favorite-service** | `article_favorites` | Favorite/unfavorite, batch favorite-count, per-user favorite state, list of a user's favorited article ids. |
| **comment-service** | `comments` | Comment CRUD, comment read model with author follow-flags. Resolves article-by-slug via article-service, follow-flags via profile-service, authorization via user-auth. |
| **api-gateway / BFF** | none | Restores the original public REST endpoints + DGS GraphQL schema; routes and recomposes read models. |

All previously-joined tables (`articles` + `article_tags` + `tags` + `users` +
`article_favorites` joined in `ArticleReadService.xml`; `comments` + `users` joined in
`CommentReadService.xml`; `follows` joined in `UserRelationshipQueryService.xml`) are now in
**separate databases**. Cross-domain data MUST be resolved via network calls (see
composition strategy), never via SQL joins.

## Composition strategy (CHOSEN: synchronous fan-out)

We use **synchronous HTTP fan-out**, NOT event-driven replication. Rationale: preserves
strong read-after-write consistency the monolith had (favorite immediately reflected in
`favoritesCount`/`favorited`), keeps children simple (no message broker, no projection
tables), and the read volume here is small. Event schemas are therefore **not used** in this
migration (the `events/` option is intentionally omitted).

**article-service is the article read-model composer.** When building an `ArticleData`
(`GET /articles`, `GET /articles/{slug}`, `GET /articles/feed`, and the response of
create/update), article-service:

1. Loads base article + tagList from its own DB.
2. Calls **user-auth-service** `GET /internal/users/{id}` to get the author `AuthorRef`
   `{id, username, bio, image}` (replaces the `left join users`).
3. Calls **favorite-service** `POST /internal/favorites/counts` (batch) for `favoritesCount`
   and, when a current user is present, `POST /internal/favorites/status` for the `favorited`
   flag (replaces `article_favorites` joins + `ArticleFavoritesReadService`).
4. Calls **profile-service** `POST /internal/follows/among` (batch) / `GET /internal/follows`
   for the author `following` flag (replaces `UserRelationshipQueryService`).
5. For `feed`, calls **profile-service** `GET /internal/follows/followed?userId=` to get the
   set of followed author ids, then queries its own `articles` by `user_id in (...)`.

The **api-gateway** does NOT re-compose article payloads itself — it delegates whole
`ArticleData` composition to article-service and only:
- routes public REST paths to the owning service,
- for `POST/DELETE /articles/{slug}/favorite`: calls favorite-service to mutate state, then
  asks article-service for the recomposed `ArticleData` (favorite endpoints in the new design
  return favorite **state only**; the gateway recomposes the full article via article-service),
- for `/articles/{slug}/comments`: resolves nothing itself — comment-service resolves the
  article by slug via article-service,
- performs GraphQL datafetch orchestration (which spans services) using the same per-service
  REST contracts.

The **current user identity** is propagated to every service by forwarding the
`Authorization: Bearer <jwt>` header. Every service validates the JWT locally using the shared
signing key (see `jwt-contract.md`) — no network hop for auth.

## Shared DTOs

See `dto/` — `AuthorRef` is the canonical author projection embedded by non-user services
instead of joining `users`. Also documents `ArticleData`, `ProfileData`, `CommentData`,
`UserData`, `UserWithToken`, `ArticleDataList` payload shapes that MUST be reproduced exactly.

## OpenAPI specs

See `openapi/*.yaml` — one per service. Public (`/...`) and internal (`/internal/...`)
endpoints are both specified. Internal endpoints are service-to-service only and are NOT
exposed by the gateway.

## Per-service migrations

See `migrations/<service>/` — each service owns only its tables, split out of the original
`db/migration/V1__create_tables.sql` + `V2__seed_data.sql`.

## Extraction / deployment order

Because of the runtime call graph, bring services up in dependency order:

```
user-auth-service  ->  profile-service  ->  article-service  ->  favorite-service  ->  comment-service  ->  api-gateway
```

- user-auth has no outbound domain deps → first.
- profile depends on user-auth (identity) → second.
- article depends on user-auth + favorite + profile → but favorite/comment depend on article
  for slug resolution, so article is deployed before favorite/comment. At runtime article ↔
  favorite is a cycle (article reads counts; favorite reads nothing from article) — article
  calls favorite, favorite does not call article, so no true cycle.
- favorite depends on article (slug→id is done by the gateway/article, favorite stores ids).
- comment depends on article (slug) + profile (follow flags) + user-auth (authz).
- gateway depends on all.

See `../MIGRATION_README.md` for the full data-ownership + deployment writeup.

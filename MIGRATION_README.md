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

`docker-compose.yml` (added in the integration phase) wires every service + its database + the
gateway. The gateway exposes the original public API on port 8080, identical to the monolith.

# Migration Plan: Monolith → Microservices (Strangler-Fig)

This document is the top-level tracking artifact for decomposing the RealWorld Spring
Boot monolith into a set of microservices, coordinated by one orchestrator Devin session
that spawns one child session per phase.

## Current state (monolith)

- Single Spring Boot 2.6.3 app, Java 11, Gradle, MyBatis, Flyway, one SQLite DB (`dev.db`).
- Entry point: `src/main/java/io/spring/RealWorldApplication.java`.
- Bounded contexts co-located in one process: `user`, `article`, `comment`, `favorite`,
  `tag`, social-graph (`follows`), plus JWT auth and read-composition query services.
- REST routes consumed by the Next.js frontend (`frontend/`) and the Selenium E2E suite:
  `/users`, `/user`, `/profiles`, `/articles`, `/articles/feed`, `/articles/{slug}`,
  `/articles/{slug}/comments`, `/articles/{slug}/favorite`, `/tags`.

## Target architecture

One service per bounded context, each an independent Spring Boot app with its **own
schema/Flyway migrations and its own SQLite DB file**. Cross-context relationships that
were foreign keys in the monolith become **referenced IDs** resolved via API composition.

```
                       ┌──────────────────────────┐
   frontend :3000 ───▶ │  gateway / BFF  :8080     │  (unchanged public routes)
   selenium tests      └─────────────┬────────────┘
                                     │  routes + JWT pass-through
        ┌───────────────┬────────────┼───────────────┬───────────────┐
        ▼               ▼            ▼                ▼               ▼
  user-service     tag-service  favorite-service profile-service  comment-service
   (users,          (tags,       (article_        (follows)        (comments)
    JWT issue/       article_      favorites)                       │
    verify)          tags)              ▲                           │
        ▲                               │                           │
        └──────────── article-service (articles) ──── API composition ─┘
             (feed/list/detail read composition = fan-out to the services above)
```

- **Shared library** `shared-contracts` (Gradle subproject `io.spring.contracts`):
  - `JwtVerifier` / `JwtTokens` — verify a bearer token and extract the user id, so every
    service authenticates requests without touching the `users` table.
  - `UserSummary`, `ProfileData` DTOs — the author/profile projection shared across services.
  - Inter-service client interfaces (`UserServiceClient`, `ProfileServiceClient`,
    `FavoriteServiceClient`, `TagServiceClient`, `CommentServiceClient`) — the contracts
    article-service and comment-service call for read composition.
- **Gateway / BFF** fronts the existing routes so the frontend and Selenium tests keep
  working **unchanged** (same paths, same JSON response shapes on `:8080`).

## Per-service schema ownership

| Service          | Owns tables                          | Referenced IDs (no FK) |
|------------------|--------------------------------------|------------------------|
| user-service     | `users`                              | —                      |
| tag-service      | `tags`, `article_tags`               | `article_id`           |
| favorite-service | `article_favorites`                  | `article_id`, `user_id`|
| profile-service  | `follows`                            | `user_id`, `follow_id` |
| comment-service  | `comments`                           | `article_id`, `user_id`|
| article-service  | `articles`                           | `user_id` (author)     |

## Read composition (replaces `ArticleQueryService.fillExtraInfo` + MyBatis joins)

The monolith's `ArticleReadService.xml` joins and `fillExtraInfo` / `setIsFollowingAuthor`
become **API composition** performed by article-service:

- favorites count + `favorited` flag  → favorite-service
- `following` flag for the author     → profile-service (`isFollowing`)
- `tagList`                            → tag-service
- author `ProfileData`                 → user-service (+ profile-service for `following`)

The feed / list / detail endpoints must return **byte-for-byte the same response shape** as
the monolith so the frontend and Selenium tests pass unchanged.

## Phased execution plan (orchestrator-coordinated)

Ordering is enforced by the orchestrator; each phase is one child session that opens a PR,
which is merged before dependents start. After each merge the orchestrator runs
`./gradlew build test`.

| Phase | Service                         | Depends on (must be merged) | Parallel with |
|-------|---------------------------------|-----------------------------|---------------|
| 0     | user-service + `shared-contracts` wiring + gateway | bootstrap        | —             |
| 1     | tag-service                     | 0                           | 2, 3          |
| 2     | favorite-service                | 0                           | 1, 3          |
| 3     | profile-service (social graph)  | 0                           | 1, 2          |
| 4     | comment-service                 | 1, 2                        | —             |
| 5     | article-service + read composition | 3, 4                     | —             |
| 6     | E2E acceptance (`seleniumTest`) | 5                           | —             |

- **Phase 0** is BLOCKING and must finish + merge before Phases 1–3.
- **Phases 1, 2, 3** run in PARALLEL (independent bounded contexts).
- **Phase 4** starts after Phases 1 & 2 merge.
- **Phase 5** starts after Phases 3 & 4 merge.
- **Phase 6** (E2E) runs only after Phase 5 merges.

## Acceptance gate

Migration is complete only when `./gradlew seleniumTest` passes against the composed system
(frontend :3000, gateway :8080) and `build/reports/selenium/ExtentReport.html` shows all
core RealWorld flows green: register/login, create article, view feed, favorite/unfavorite,
follow/unfollow, add/delete comment, filter by tag.

## Deliverables

Multi-module Gradle layout: `shared-contracts`, a gateway/BFF, and `user-service`,
`tag-service`, `favorite-service`, `profile-service`, `comment-service`, `article-service`,
each with its own Flyway schema, plus a passing Selenium E2E run.

## Status tracker

- [ ] Bootstrap: MIGRATION_PLAN.md + `shared-contracts` module
- [ ] Phase 0 — user-service + gateway
- [ ] Phase 1 — tag-service
- [ ] Phase 2 — favorite-service
- [ ] Phase 3 — profile-service
- [ ] Phase 4 — comment-service
- [ ] Phase 5 — article-service + read composition
- [ ] Phase 6 — E2E acceptance (`seleniumTest` green)

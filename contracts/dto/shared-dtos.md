# Shared DTOs (FROZEN)

These payload shapes MUST be reproduced **exactly** so the public REST + GraphQL API is
unchanged from the monolith. JSON field names, nesting, and root wrappers are normative.

## AuthorRef (NEW — shared projection)

Canonical author projection that non-user services embed/cache instead of joining `users`.
Returned by user-auth-service `GET /internal/users/{id}` and `POST /internal/users/batch`.

```json
{ "id": "uuid", "username": "jake", "bio": "I work at state farm", "image": "https://..." }
```

- `id` is used only internally (to correlate follow flags etc.). When projected into a public
  `author`/`ProfileData` payload the `id` is dropped (`@JsonIgnore`) and `following` is added.

## ProfileData (public `author` and `profile`)

```json
{ "username": "jake", "bio": "...", "image": "https://...", "following": false }
```

- Java: `id` is `@JsonIgnore` (present in-process, never serialized). `following` is a boolean.
- Emitted inside `{ "profile": { ... } }` for the profile endpoints, and inline as
  `"author": { ... }` inside `ArticleData` / `CommentData`.

## UserData (in-process projection)

```json
{ "id": "uuid", "email": "e@x.com", "username": "jake", "bio": "...", "image": "https://..." }
```

## UserWithToken (public `user` payload)

Root-wrapped as `{ "user": { ... } }`. NOTE: `id` is NOT serialized.

```json
{ "email": "e@x.com", "username": "jake", "bio": "...", "image": "https://...", "token": "jwt" }
```

## ArticleData (public `article`)

Root-wrapped `{ "article": { ... } }` for single-article endpoints; listed under
`ArticleDataList` for collections.

```json
{
  "slug": "how-to-train-your-dragon",
  "title": "How to train your dragon",
  "description": "Ever wonder how?",
  "body": "It takes a Jacobian",
  "tagList": ["dragons", "training"],
  "createdAt": "2016-02-18T03:22:56.637Z",
  "updatedAt": "2016-02-18T03:48:35.824Z",
  "favorited": false,
  "favoritesCount": 0,
  "author": { "username": "jake", "bio": "...", "image": "...", "following": false }
}
```

- Java field `id` exists in-process (`Node` cursor) but the RealWorld JSON does not include a
  top-level article `id` — keep serialization identical to the monolith (`ArticleData` has
  `id` as a normal field; the monolith serializes it — reproduce exactly whatever the monolith
  emits; do not add/remove fields).
- `createdAt`/`updatedAt` are Joda `DateTime` serialized as ISO-8601.
- `favorited` / `favoritesCount` / `author.following` are the COMPOSED fields (see composition
  strategy). Defaults when anonymous: `favorited=false`, `following=false`; `favoritesCount`
  is always the real count.

## ArticleDataList (public article collections)

```json
{ "articles": [ ArticleData... ], "articlesCount": 12 }
```

## ArticleFavoriteCount (internal)

```json
{ "id": "articleId", "count": 3 }
```

## CommentData (public `comment` / `comments`)

Single: `{ "comment": { ... } }`; list: `{ "comments": [ ... ] }`.

```json
{
  "id": "uuid",
  "body": "text",
  "createdAt": "2016-...Z",
  "updatedAt": "2016-...Z",
  "author": { "username": "jake", "bio": "...", "image": "...", "following": false }
}
```

- Java field `articleId` is `@JsonIgnore` (not serialized).

## Error payloads (unchanged)

- Validation: `{ "errors": { "field": ["message", ...] } }` (422) via `ErrorResource`.
- 401 for invalid/missing auth on protected routes, 403 for `NoAuthorizationException`,
  404 for `ResourceNotFoundException`.

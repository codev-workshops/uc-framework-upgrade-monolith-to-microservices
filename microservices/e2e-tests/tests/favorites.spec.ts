import { test, expect } from "@playwright/test";

let authToken: string;

test.beforeAll(async ({ request }) => {
  const response = await request.post("/users/login", {
    data: {
      user: {
        email: "jane@example.com",
        password: "password123",
      },
    },
  });
  const body = await response.json();
  authToken = body.user.token;
});

test.describe("Favorites API", () => {
  const articleSlug = "microservices-architecture-guide";

  test("POST /articles/:slug/favorite - favorite an article", async ({
    request,
  }) => {
    const response = await request.post(`/articles/${articleSlug}/favorite`, {
      headers: {
        Authorization: `Token ${authToken}`,
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.article).toBeDefined();
    expect(body.article.favorited).toBe(true);
    expect(body.article.favoritesCount).toBeGreaterThanOrEqual(1);
  });

  test("DELETE /articles/:slug/favorite - unfavorite an article", async ({
    request,
  }) => {
    const response = await request.delete(`/articles/${articleSlug}/favorite`, {
      headers: {
        Authorization: `Token ${authToken}`,
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.article).toBeDefined();
    expect(body.article.favorited).toBe(false);
  });

  test("GET /articles?favorited=:username - list favorited articles", async ({
    request,
  }) => {
    const response = await request.get("/articles?favorited=janedoe");

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.articles).toBeDefined();
    expect(Array.isArray(body.articles)).toBeTruthy();
  });
});

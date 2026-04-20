import { test, expect } from "@playwright/test";

let authToken: string;

test.beforeAll(async ({ request }) => {
  const response = await request.post("/users/login", {
    data: {
      user: {
        email: "john@example.com",
        password: "password123",
      },
    },
  });
  const body = await response.json();
  authToken = body.user.token;
});

test.describe("Feed API", () => {
  test("GET /articles/feed - get user feed (authenticated)", async ({
    request,
  }) => {
    const response = await request.get("/articles/feed", {
      headers: {
        Authorization: `Token ${authToken}`,
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.articles).toBeDefined();
    expect(Array.isArray(body.articles)).toBeTruthy();
    expect(body.articlesCount).toBeDefined();
  });

  test("GET /articles/feed - feed requires authentication", async ({
    request,
  }) => {
    const response = await request.get("/articles/feed");

    expect(response.status()).toBe(401);
  });

  test("GET /articles/feed - feed returns articles from followed users", async ({
    request,
  }) => {
    const response = await request.get("/articles/feed", {
      headers: {
        Authorization: `Token ${authToken}`,
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.articles).toBeDefined();
    for (const article of body.articles) {
      expect(article.author).toBeDefined();
      expect(article.author.username).toBeDefined();
    }
  });
});

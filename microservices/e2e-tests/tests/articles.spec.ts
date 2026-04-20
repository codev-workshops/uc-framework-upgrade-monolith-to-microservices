import { test, expect } from "@playwright/test";

let authToken: string;
const uniqueSuffix = Date.now();

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

test.describe("Articles API", () => {
  test("GET /articles - list articles", async ({ request }) => {
    const response = await request.get("/articles");

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.articles).toBeDefined();
    expect(Array.isArray(body.articles)).toBeTruthy();
    expect(body.articlesCount).toBeDefined();
  });

  test("GET /articles - list articles with limit and offset", async ({
    request,
  }) => {
    const response = await request.get("/articles?limit=2&offset=0");

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.articles).toBeDefined();
    expect(body.articles.length).toBeLessThanOrEqual(2);
  });

  test("GET /articles - filter by tag", async ({ request }) => {
    const response = await request.get("/articles?tag=java");

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.articles).toBeDefined();
  });

  test("GET /articles - filter by author", async ({ request }) => {
    const response = await request.get("/articles?author=johndoe");

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.articles).toBeDefined();
    for (const article of body.articles) {
      expect(article.author.username).toBe("johndoe");
    }
  });

  test("POST /articles - create article", async ({ request }) => {
    const response = await request.post("/articles", {
      headers: {
        Authorization: `Token ${authToken}`,
      },
      data: {
        article: {
          title: `Test Article ${uniqueSuffix}`,
          description: "Test description",
          body: "Test article body content",
          tagList: ["test", "e2e"],
        },
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.article).toBeDefined();
    expect(body.article.title).toBe(`Test Article ${uniqueSuffix}`);
    expect(body.article.description).toBe("Test description");
  });

  test("GET /articles/:slug - get single article", async ({ request }) => {
    const response = await request.get(
      "/articles/getting-started-with-spring-boot",
    );

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.article).toBeDefined();
    expect(body.article.slug).toBe("getting-started-with-spring-boot");
    expect(body.article.title).toBe("Getting Started with Spring Boot");
  });

  test("PUT /articles/:slug - update article", async ({ request }) => {
    const createResponse = await request.post("/articles", {
      headers: {
        Authorization: `Token ${authToken}`,
      },
      data: {
        article: {
          title: `Update Test ${uniqueSuffix}`,
          description: "To be updated",
          body: "Original body",
          tagList: ["test"],
        },
      },
    });
    const createBody = await createResponse.json();
    const slug = createBody.article.slug;

    const response = await request.put(`/articles/${slug}`, {
      headers: {
        Authorization: `Token ${authToken}`,
      },
      data: {
        article: {
          body: "Updated body content",
        },
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.article.body).toBe("Updated body content");
  });

  test("DELETE /articles/:slug - delete article", async ({ request }) => {
    const createResponse = await request.post("/articles", {
      headers: {
        Authorization: `Token ${authToken}`,
      },
      data: {
        article: {
          title: `Delete Test ${uniqueSuffix}`,
          description: "To be deleted",
          body: "Will be deleted",
          tagList: ["test"],
        },
      },
    });
    const createBody = await createResponse.json();
    const slug = createBody.article.slug;

    const response = await request.delete(`/articles/${slug}`, {
      headers: {
        Authorization: `Token ${authToken}`,
      },
    });

    expect(response.status()).toBe(200);
  });
});

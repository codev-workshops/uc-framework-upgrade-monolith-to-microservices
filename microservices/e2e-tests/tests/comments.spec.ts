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

test.describe("Comments API", () => {
  const articleSlug = "getting-started-with-spring-boot";

  test("GET /articles/:slug/comments - list comments", async ({ request }) => {
    const response = await request.get(`/articles/${articleSlug}/comments`);

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.comments).toBeDefined();
    expect(Array.isArray(body.comments)).toBeTruthy();
  });

  test("POST /articles/:slug/comments - add comment", async ({ request }) => {
    const response = await request.post(`/articles/${articleSlug}/comments`, {
      headers: {
        Authorization: `Token ${authToken}`,
      },
      data: {
        comment: {
          body: "This is a test comment from e2e tests",
        },
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.comment).toBeDefined();
    expect(body.comment.body).toBe("This is a test comment from e2e tests");
    expect(body.comment.author).toBeDefined();
  });

  test("DELETE /articles/:slug/comments/:id - delete comment", async ({
    request,
  }) => {
    const createResponse = await request.post(
      `/articles/${articleSlug}/comments`,
      {
        headers: {
          Authorization: `Token ${authToken}`,
        },
        data: {
          comment: {
            body: "Comment to be deleted",
          },
        },
      },
    );
    const createBody = await createResponse.json();
    const commentId = createBody.comment.id;

    const response = await request.delete(
      `/articles/${articleSlug}/comments/${commentId}`,
      {
        headers: {
          Authorization: `Token ${authToken}`,
        },
      },
    );

    expect(response.status()).toBe(200);
  });
});

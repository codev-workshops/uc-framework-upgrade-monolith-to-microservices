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

test.describe("Profiles API", () => {
  test("GET /profiles/:username - get profile", async ({ request }) => {
    const response = await request.get("/profiles/janedoe");

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.profile).toBeDefined();
    expect(body.profile.username).toBe("janedoe");
    expect(body.profile.bio).toBeDefined();
    expect(body.profile.image).toBeDefined();
  });

  test("POST /profiles/:username/follow - follow user", async ({
    request,
  }) => {
    const response = await request.post("/profiles/bobsmith/follow", {
      headers: {
        Authorization: `Token ${authToken}`,
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.profile).toBeDefined();
    expect(body.profile.username).toBe("bobsmith");
    expect(body.profile.following).toBe(true);
  });

  test("DELETE /profiles/:username/follow - unfollow user", async ({
    request,
  }) => {
    const response = await request.delete("/profiles/bobsmith/follow", {
      headers: {
        Authorization: `Token ${authToken}`,
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.profile).toBeDefined();
    expect(body.profile.username).toBe("bobsmith");
    expect(body.profile.following).toBe(false);
  });
});

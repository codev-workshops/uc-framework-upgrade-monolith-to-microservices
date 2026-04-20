import { test, expect } from "@playwright/test";

const uniqueSuffix = Date.now();

test.describe("Authentication API", () => {
  test("POST /users - register a new user", async ({ request }) => {
    const response = await request.post("/users", {
      data: {
        user: {
          username: `testuser_${uniqueSuffix}`,
          email: `testuser_${uniqueSuffix}@example.com`,
          password: "password123",
        },
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.user).toBeDefined();
    expect(body.user.username).toBe(`testuser_${uniqueSuffix}`);
    expect(body.user.email).toBe(`testuser_${uniqueSuffix}@example.com`);
    expect(body.user.token).toBeDefined();
  });

  test("POST /users/login - login with existing user", async ({ request }) => {
    const response = await request.post("/users/login", {
      data: {
        user: {
          email: "john@example.com",
          password: "password123",
        },
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.user).toBeDefined();
    expect(body.user.email).toBe("john@example.com");
    expect(body.user.token).toBeDefined();
  });

  test("GET /user - get current user", async ({ request }) => {
    const loginResponse = await request.post("/users/login", {
      data: {
        user: {
          email: "john@example.com",
          password: "password123",
        },
      },
    });
    const loginBody = await loginResponse.json();
    const token = loginBody.user.token;

    const response = await request.get("/user", {
      headers: {
        Authorization: `Token ${token}`,
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.user).toBeDefined();
    expect(body.user.email).toBe("john@example.com");
  });

  test("PUT /user - update current user", async ({ request }) => {
    const loginResponse = await request.post("/users/login", {
      data: {
        user: {
          email: "john@example.com",
          password: "password123",
        },
      },
    });
    const loginBody = await loginResponse.json();
    const token = loginBody.user.token;

    const response = await request.put("/user", {
      headers: {
        Authorization: `Token ${token}`,
      },
      data: {
        user: {
          bio: "Updated bio from e2e test",
        },
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.user).toBeDefined();
    expect(body.user.bio).toBe("Updated bio from e2e test");
  });
});

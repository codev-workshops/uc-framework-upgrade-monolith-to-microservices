import { test, expect } from "@playwright/test";

test.describe("Tags API", () => {
  test("GET /tags - list all tags", async ({ request }) => {
    const response = await request.get("/tags");

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.tags).toBeDefined();
    expect(Array.isArray(body.tags)).toBeTruthy();
    expect(body.tags.length).toBeGreaterThan(0);
  });
});

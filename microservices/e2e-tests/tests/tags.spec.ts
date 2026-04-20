import { test, expect } from '@playwright/test';

const API_URL = process.env.API_URL || 'http://localhost:8080';

test.describe('Tags', () => {
  test('should list tags', async ({ request }) => {
    const response = await request.get(`${API_URL}/tags`);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.tags).toBeDefined();
    expect(Array.isArray(body.tags)).toBe(true);
  });
});

import { test, expect } from '@playwright/test';

const API_URL = process.env.API_URL || 'http://localhost:8080';
const uniqueSuffix = Date.now().toString();

test.describe('Feed', () => {
  test('should get global feed', async ({ request }) => {
    const response = await request.get(`${API_URL}/articles`);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.articles).toBeDefined();
  });

  test('should get personal feed (requires auth)', async ({ request }) => {
    const username = `feeduser${uniqueSuffix}${Math.floor(Math.random() * 10000)}`;
    const regResponse = await request.post(`${API_URL}/users`, {
      data: {
        user: {
          email: `${username}@example.com`,
          username: username,
          password: 'password123',
        },
      },
    });
    const { user } = await regResponse.json();

    const response = await request.get(`${API_URL}/articles/feed`, {
      headers: { Authorization: `Token ${user.token}` },
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.articles).toBeDefined();
    expect(body.articlesCount).toBeDefined();
  });
});

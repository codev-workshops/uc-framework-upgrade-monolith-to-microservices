import { test, expect } from '@playwright/test';

const API_URL = process.env.API_URL || 'http://localhost:8080';
const uniqueSuffix = Date.now().toString();

test.describe('Authentication', () => {
  const testUser = {
    email: `test${uniqueSuffix}@example.com`,
    username: `testuser${uniqueSuffix}`,
    password: 'password123',
  };

  test('should register a new user', async ({ request }) => {
    const response = await request.post(`${API_URL}/users`, {
      data: {
        user: testUser,
      },
    });
    expect(response.status()).toBe(201);
    const body = await response.json();
    expect(body.user.email).toBe(testUser.email);
    expect(body.user.username).toBe(testUser.username);
    expect(body.user.token).toBeTruthy();
  });

  test('should login an existing user', async ({ request }) => {
    // Register first
    await request.post(`${API_URL}/users`, {
      data: { user: testUser },
    });

    const response = await request.post(`${API_URL}/users/login`, {
      data: {
        user: {
          email: testUser.email,
          password: testUser.password,
        },
      },
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.user.email).toBe(testUser.email);
    expect(body.user.token).toBeTruthy();
  });

  test('should get current user', async ({ request }) => {
    // Register and get token
    const regResponse = await request.post(`${API_URL}/users`, {
      data: { user: testUser },
    });
    const { user } = await regResponse.json();

    const response = await request.get(`${API_URL}/user`, {
      headers: { Authorization: `Token ${user.token}` },
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.user.email).toBe(testUser.email);
  });

  test('should update user profile', async ({ request }) => {
    // Register and get token
    const regResponse = await request.post(`${API_URL}/users`, {
      data: { user: testUser },
    });
    const { user } = await regResponse.json();

    const response = await request.put(`${API_URL}/user`, {
      headers: { Authorization: `Token ${user.token}` },
      data: {
        user: {
          bio: 'Updated bio',
        },
      },
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.user.bio).toBe('Updated bio');
  });
});

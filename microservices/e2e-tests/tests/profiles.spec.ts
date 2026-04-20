import { test, expect } from '@playwright/test';

const API_URL = process.env.API_URL || 'http://localhost:8080';
const uniqueSuffix = Date.now().toString();

test.describe('Profiles', () => {
  test('should get a user profile', async ({ request }) => {
    const username = `profile${uniqueSuffix}${Math.floor(Math.random() * 10000)}`;
    await request.post(`${API_URL}/users`, {
      data: {
        user: {
          email: `${username}@example.com`,
          username: username,
          password: 'password123',
        },
      },
    });

    const response = await request.get(`${API_URL}/profiles/${username}`);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.profile.username).toBe(username);
  });

  test('should follow a user', async ({ request }) => {
    const user1Name = `follower${uniqueSuffix}${Math.floor(Math.random() * 10000)}`;
    const user2Name = `followee${uniqueSuffix}${Math.floor(Math.random() * 10000)}`;

    const reg1 = await request.post(`${API_URL}/users`, {
      data: {
        user: {
          email: `${user1Name}@example.com`,
          username: user1Name,
          password: 'password123',
        },
      },
    });
    const { user: user1 } = await reg1.json();

    await request.post(`${API_URL}/users`, {
      data: {
        user: {
          email: `${user2Name}@example.com`,
          username: user2Name,
          password: 'password123',
        },
      },
    });

    const response = await request.post(`${API_URL}/profiles/${user2Name}/follow`, {
      headers: { Authorization: `Token ${user1.token}` },
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.profile.following).toBe(true);
  });

  test('should unfollow a user', async ({ request }) => {
    const user1Name = `unfollower${uniqueSuffix}${Math.floor(Math.random() * 10000)}`;
    const user2Name = `unfollowee${uniqueSuffix}${Math.floor(Math.random() * 10000)}`;

    const reg1 = await request.post(`${API_URL}/users`, {
      data: {
        user: {
          email: `${user1Name}@example.com`,
          username: user1Name,
          password: 'password123',
        },
      },
    });
    const { user: user1 } = await reg1.json();

    await request.post(`${API_URL}/users`, {
      data: {
        user: {
          email: `${user2Name}@example.com`,
          username: user2Name,
          password: 'password123',
        },
      },
    });

    await request.post(`${API_URL}/profiles/${user2Name}/follow`, {
      headers: { Authorization: `Token ${user1.token}` },
    });

    const response = await request.delete(`${API_URL}/profiles/${user2Name}/follow`, {
      headers: { Authorization: `Token ${user1.token}` },
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.profile.following).toBe(false);
  });
});

import { test, expect } from '@playwright/test';

const API_URL = process.env.API_URL || 'http://localhost:8080';
const uniqueSuffix = Date.now().toString();

async function createUserAndGetToken(request: any): Promise<string> {
  const response = await request.post(`${API_URL}/users`, {
    data: {
      user: {
        email: `fav${uniqueSuffix}${Math.random()}@example.com`,
        username: `favuser${uniqueSuffix}${Math.floor(Math.random() * 10000)}`,
        password: 'password123',
      },
    },
  });
  const body = await response.json();
  return body.user.token;
}

async function createArticle(request: any, token: string): Promise<string> {
  const response = await request.post(`${API_URL}/articles`, {
    headers: { Authorization: `Token ${token}` },
    data: {
      article: {
        title: `Fav Article ${uniqueSuffix}${Math.random()}`,
        description: 'For favorites',
        body: 'Article body',
        tagList: [],
      },
    },
  });
  const body = await response.json();
  return body.article.slug;
}

test.describe('Favorites', () => {
  test('should favorite an article', async ({ request }) => {
    const token = await createUserAndGetToken(request);
    const slug = await createArticle(request, token);

    const response = await request.post(`${API_URL}/articles/${slug}/favorite`, {
      headers: { Authorization: `Token ${token}` },
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.article.favorited).toBe(true);
    expect(body.article.favoritesCount).toBe(1);
  });

  test('should unfavorite an article', async ({ request }) => {
    const token = await createUserAndGetToken(request);
    const slug = await createArticle(request, token);

    await request.post(`${API_URL}/articles/${slug}/favorite`, {
      headers: { Authorization: `Token ${token}` },
    });

    const response = await request.delete(`${API_URL}/articles/${slug}/favorite`, {
      headers: { Authorization: `Token ${token}` },
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.article.favorited).toBe(false);
    expect(body.article.favoritesCount).toBe(0);
  });
});

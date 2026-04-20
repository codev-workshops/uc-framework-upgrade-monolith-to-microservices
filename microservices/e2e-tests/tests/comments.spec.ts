import { test, expect } from '@playwright/test';

const API_URL = process.env.API_URL || 'http://localhost:8080';
const uniqueSuffix = Date.now().toString();

async function createUserAndGetToken(request: any): Promise<string> {
  const response = await request.post(`${API_URL}/users`, {
    data: {
      user: {
        email: `comment${uniqueSuffix}${Math.random()}@example.com`,
        username: `commentuser${uniqueSuffix}${Math.floor(Math.random() * 10000)}`,
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
        title: `Comment Article ${uniqueSuffix}${Math.random()}`,
        description: 'For comments',
        body: 'Article body',
        tagList: [],
      },
    },
  });
  const body = await response.json();
  return body.article.slug;
}

test.describe('Comments', () => {
  test('should add a comment to an article', async ({ request }) => {
    const token = await createUserAndGetToken(request);
    const slug = await createArticle(request, token);

    const response = await request.post(`${API_URL}/articles/${slug}/comments`, {
      headers: { Authorization: `Token ${token}` },
      data: {
        comment: { body: 'Great article!' },
      },
    });
    expect(response.status()).toBe(201);
    const body = await response.json();
    expect(body.comment.body).toBe('Great article!');
  });

  test('should list comments for an article', async ({ request }) => {
    const token = await createUserAndGetToken(request);
    const slug = await createArticle(request, token);

    await request.post(`${API_URL}/articles/${slug}/comments`, {
      headers: { Authorization: `Token ${token}` },
      data: { comment: { body: 'First comment' } },
    });

    const response = await request.get(`${API_URL}/articles/${slug}/comments`);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.comments).toBeDefined();
    expect(body.comments.length).toBeGreaterThanOrEqual(1);
  });

  test('should delete a comment', async ({ request }) => {
    const token = await createUserAndGetToken(request);
    const slug = await createArticle(request, token);

    const createResponse = await request.post(`${API_URL}/articles/${slug}/comments`, {
      headers: { Authorization: `Token ${token}` },
      data: { comment: { body: 'To be deleted' } },
    });
    const created = await createResponse.json();

    const response = await request.delete(
      `${API_URL}/articles/${slug}/comments/${created.comment.id}`,
      { headers: { Authorization: `Token ${token}` } }
    );
    expect(response.status()).toBe(204);
  });
});

import { test, expect } from '@playwright/test';

const API_URL = process.env.API_URL || 'http://localhost:8080';
const uniqueSuffix = Date.now().toString();

async function createUserAndGetToken(request: any): Promise<string> {
  const response = await request.post(`${API_URL}/users`, {
    data: {
      user: {
        email: `article${uniqueSuffix}${Math.random()}@example.com`,
        username: `articleuser${uniqueSuffix}${Math.floor(Math.random() * 10000)}`,
        password: 'password123',
      },
    },
  });
  const body = await response.json();
  return body.user.token;
}

test.describe('Articles', () => {
  test('should create an article', async ({ request }) => {
    const token = await createUserAndGetToken(request);
    const response = await request.post(`${API_URL}/articles`, {
      headers: { Authorization: `Token ${token}` },
      data: {
        article: {
          title: `Test Article ${uniqueSuffix}`,
          description: 'Test description',
          body: 'Test body content',
          tagList: ['test', 'e2e'],
        },
      },
    });
    expect(response.status()).toBe(201);
    const body = await response.json();
    expect(body.article.title).toBe(`Test Article ${uniqueSuffix}`);
    expect(body.article.tagList).toContain('test');
  });

  test('should list articles', async ({ request }) => {
    const response = await request.get(`${API_URL}/articles`);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.articles).toBeDefined();
    expect(body.articlesCount).toBeDefined();
  });

  test('should get article by slug', async ({ request }) => {
    const token = await createUserAndGetToken(request);
    const createResponse = await request.post(`${API_URL}/articles`, {
      headers: { Authorization: `Token ${token}` },
      data: {
        article: {
          title: `Slug Test ${uniqueSuffix}`,
          description: 'Test',
          body: 'Test body',
          tagList: [],
        },
      },
    });
    const created = await createResponse.json();
    const slug = created.article.slug;

    const response = await request.get(`${API_URL}/articles/${slug}`);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.article.slug).toBe(slug);
  });

  test('should update an article', async ({ request }) => {
    const token = await createUserAndGetToken(request);
    const createResponse = await request.post(`${API_URL}/articles`, {
      headers: { Authorization: `Token ${token}` },
      data: {
        article: {
          title: `Update Test ${uniqueSuffix}`,
          description: 'Original',
          body: 'Original body',
          tagList: [],
        },
      },
    });
    const created = await createResponse.json();
    const slug = created.article.slug;

    const response = await request.put(`${API_URL}/articles/${slug}`, {
      headers: { Authorization: `Token ${token}` },
      data: {
        article: { description: 'Updated description' },
      },
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.article.description).toBe('Updated description');
  });

  test('should delete an article', async ({ request }) => {
    const token = await createUserAndGetToken(request);
    const createResponse = await request.post(`${API_URL}/articles`, {
      headers: { Authorization: `Token ${token}` },
      data: {
        article: {
          title: `Delete Test ${uniqueSuffix}`,
          description: 'To be deleted',
          body: 'Delete me',
          tagList: [],
        },
      },
    });
    const created = await createResponse.json();
    const slug = created.article.slug;

    const response = await request.delete(`${API_URL}/articles/${slug}`, {
      headers: { Authorization: `Token ${token}` },
    });
    expect(response.status()).toBe(204);
  });
});

import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 30000,
  retries: 1,
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
    headless: true,
    extraHTTPHeaders: {
      'Accept': 'application/json',
    },
  },
  reporter: [
    ['html'],
    ['json', { outputFile: 'results/test-report.json' }],
  ],
});

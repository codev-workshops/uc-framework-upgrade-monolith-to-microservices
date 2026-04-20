import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./tests",
  timeout: 30000,
  retries: 1,
  use: {
    baseURL: process.env.API_URL || "http://localhost:8080",
    extraHTTPHeaders: {
      "Content-Type": "application/json",
    },
  },
  reporter: [["list"], ["html", { open: "never" }]],
});

import { defineConfig, devices } from '@playwright/test';
import { loadEnv } from 'vite';
import { fileURLToPath } from 'node:url';
const env = loadEnv('development', fileURLToPath(new URL('../', import.meta.url)), 'WEB_');
const baseURL = `http://127.0.0.1:${env.WEB_PORT || 5173}`;
export default defineConfig({
  testDir: 'tests/e2e',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: 'list',
  use: {
    baseURL,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'desktop', use: { ...devices['Desktop Edge'], channel: 'msedge' } }],
  webServer: {
    command: 'npm run dev',
    url: baseURL,
    reuseExistingServer: !process.env.CI,
  },
});

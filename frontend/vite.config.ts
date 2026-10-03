import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath } from 'node:url';
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, fileURLToPath(new URL('../', import.meta.url)), 'APP_');
  return {
    plugins: [vue()],
    server: {
      port: 5173,
      strictPort: true,
      proxy: {
        '/backend': {
          target: `http://127.0.0.1:${env.APP_PORT || 8081}`,
          changeOrigin: false,
          rewrite: (path) => path.replace(/^\/backend/, ''),
        },
      },
    },
  };
});

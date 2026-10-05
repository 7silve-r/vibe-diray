import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath } from 'node:url';
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, fileURLToPath(new URL('../', import.meta.url)), [
    'APP_',
    'WEB_',
    'UPLOAD_',
  ]);
  return {
    plugins: [vue()],
    define: { __UPLOAD_PREFIX__: JSON.stringify(env.UPLOAD_URL_PREFIX || '/uploads/') },
    server: {
      port: Number(env.WEB_PORT || 5173),
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

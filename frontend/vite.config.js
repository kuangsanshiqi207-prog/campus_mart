import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/user': 'http://127.0.0.1:8081',
      '/admin': {
        target: 'http://127.0.0.1:8081',
        bypass(req) {
          if (req.headers.accept && req.headers.accept.includes('text/html')) {
            return '/index.html'
          }
        }
      },
      '/uploads': 'http://127.0.0.1:8081'
    }
  }
})

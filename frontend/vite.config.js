import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue({
    template: {
      compilerOptions: {
        isCustomElement: (tag) => {
          const whitelist = ['TresCanvas', 'TresLeches', 'TresScene']
          return (/^Tres[A-Z]/.test(tag) || tag === 'primitive') && !whitelist.includes(tag)
        }
      }
    }
  })],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    host: '0.0.0.0',  // 允许外部访问（手机访问）
    port: 5173,
    cors: true,
    allowedHosts: ['.serveo.net', '.serveousercontent.com', '.local', '172.20.72.65'],
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})

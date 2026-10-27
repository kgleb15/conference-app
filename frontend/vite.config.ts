import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/organizer': 'http://localhost:8080',
      '/applications': 'http://localhost:8080',
    },
  },
})

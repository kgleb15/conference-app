import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/persons': 'http://localhost:8080',
      '/applications': 'http://localhost:8080',
      '/organizer': 'http://localhost:8080',
      '/expert': 'http://localhost:8080',
    },
  },
})

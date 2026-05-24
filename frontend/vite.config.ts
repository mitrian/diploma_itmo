import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const mkcertDir = path.resolve(__dirname, '../docker/ssl')
const mkcertCert = path.join(mkcertDir, 'localhost.pem')
const mkcertKey = path.join(mkcertDir, 'localhost-key.pem')
const mkcertHttps =
  fs.existsSync(mkcertCert) && fs.existsSync(mkcertKey)
    ? { cert: fs.readFileSync(mkcertCert), key: fs.readFileSync(mkcertKey) }
    : undefined

export default defineConfig({
  plugins: [react()],
  server: {
    https: mkcertHttps,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyRes', (proxyRes) => {
            const ct = String(proxyRes.headers['content-type'] ?? '')
            if (ct.includes('text/event-stream')) {
              proxyRes.headers['cache-control'] = 'no-cache, no-transform'
              proxyRes.headers['x-accel-buffering'] = 'no'
              delete proxyRes.headers['content-length']
            }
          })
        },
      },
    },
  },
})

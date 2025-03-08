import { defineConfig } from 'vite';
import inject from '@rollup/plugin-inject';


export default defineConfig({
  define: {
    global: 'globalThis', // ✅ Pour corriger l'erreur "global is not defined"
  },
  resolve: {
    alias: {
      process: 'process/browser', // ✅ Polyfill pour 'process'
      buffer: 'buffer',           // ✅ Polyfill pour 'Buffer'
    }
  },
  build: {
    rollupOptions: {
      plugins: [
        inject({
          global: ['globalThis', 'global'], // ✅ Injection de 'global'
          process: 'process/browser',       // ✅ Injection de 'process'
          Buffer: ['buffer', 'Buffer'],     // ✅ Injection de 'Buffer'
        }),
      ],
    }
  }
});

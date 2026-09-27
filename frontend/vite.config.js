import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// During `npm run dev` (port 5173) API calls are proxied to the Spring Boot
// backend on port 8080. In production, Spring Boot serves this app's built
// files directly from the SAME origin/port, so no proxy is needed there.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: "dist",
  },
});

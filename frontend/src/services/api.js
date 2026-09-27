import axios from "axios";

// Relative base URL — works whether the app is served by Spring Boot on the
// same origin (production, http://localhost:8080) or proxied during
// `npm run dev` (vite.config.js proxies /api to the backend).
const API_BASE_URL = "/api";

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // send the session cookie on every request
  headers: {
    "Content-Type": "application/json",
  },
  // Axios automatically reads the XSRF-TOKEN cookie set by Spring Security
  // and attaches it as the X-XSRF-TOKEN header on state-changing requests.
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN",
});

// Normalize error responses so callers can read a consistent message.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ||
      error.message ||
      "Something went wrong. Please try again.";
    return Promise.reject({ ...error, message });
  }
);

export default api;

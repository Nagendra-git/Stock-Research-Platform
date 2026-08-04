import axios from "axios";

// Central place for base URL + interceptors so every service reuses one client.
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Example hook point for auth tokens / global error handling later.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error("API error:", error?.response?.status, error?.message);
    return Promise.reject(error);
  }
);

export default apiClient;

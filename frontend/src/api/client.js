import axios from "axios";

<<<<<<< HEAD
const BASE_URL = import.meta.env.VITE_API_BASE_URL || "";
=======
const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
>>>>>>> 47efb803394e528830006d2fcb4f4d9b127d64e3

export const api = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
});

// Attach the JWT (if we have one) to every outgoing request.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// If the backend ever returns 401 (expired/invalid token), force a clean logout
// instead of leaving the app stuck in a broken authenticated-looking state.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("username");
      localStorage.removeItem("walletId");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

// ---- Auth ----
export const registerUser = (payload) => api.post("/api/auth/register", payload);
export const loginUser = (payload) => api.post("/api/auth/login", payload);

// ---- Wallet ----
export const getMyWallet = () => api.get("/api/wallet/me");
export const lookupWallet = (walletId) => api.get(`/api/wallet/${walletId}`);
export const getMyQrCode = () => api.get("/api/wallet/me/qrcode");

// ---- Transactions ----
export const sendCrypto = (payload) => api.post("/api/transactions/send", payload);
export const getHistory = () => api.get("/api/transactions/history");

// ---- Market data (public) ----
export const getPrices = (coins, vsCurrencies = "usd") =>
  api.get("/api/market/prices", { params: { coins, vsCurrencies } });
export const getMarkets = (vsCurrency = "usd", perPage = 20, page = 1) =>
  api.get("/api/market/coins", { params: { vsCurrency, perPage, page } });

// ---- Trade (buy / sell / swap on the demo account, live-priced) ----
export const getQuote = (coinSymbol) => api.get(`/api/trade/quote/${coinSymbol}`);
export const getSupportedCoins = () => api.get("/api/trade/supported-coins");
export const buyCoin = (payload) => api.post("/api/trade/buy", payload);
export const sellCoin = (payload) => api.post("/api/trade/sell", payload);
export const swapCoins = (payload) => api.post("/api/trade/swap", payload);

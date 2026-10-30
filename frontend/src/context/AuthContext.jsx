import { createContext, useContext, useState, useCallback } from "react";
import { loginUser, registerUser } from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem("token"));
  const [username, setUsername] = useState(() => localStorage.getItem("username"));
  const [walletId, setWalletId] = useState(() => localStorage.getItem("walletId"));

  const persistSession = useCallback((data) => {
    localStorage.setItem("token", data.token);
    localStorage.setItem("username", data.username);
    localStorage.setItem("walletId", data.walletId || "");
    setToken(data.token);
    setUsername(data.username);
    setWalletId(data.walletId || "");
  }, []);

  const login = useCallback(
    async (usernameOrEmail, password) => {
      const { data } = await loginUser({ usernameOrEmail, password });
      persistSession(data);
      return data;
    },
    [persistSession]
  );

  const register = useCallback(
    async (username, email, password) => {
      const { data } = await registerUser({ username, email, password });
      persistSession(data);
      return data;
    },
    [persistSession]
  );

  const logout = useCallback(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("walletId");
    setToken(null);
    setUsername(null);
    setWalletId(null);
  }, []);

  const value = {
    token,
    username,
    walletId,
    isAuthenticated: Boolean(token),
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside an AuthProvider");
  return ctx;
}

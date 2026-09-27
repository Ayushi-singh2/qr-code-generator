import api from "./api";

const authService = {
  async register({ fullName, email, password, confirmPassword }) {
    const { data } = await api.post("/auth/register", {
      fullName,
      email,
      password,
      confirmPassword,
    });
    return data;
  },

  async login({ email, password }) {
    const { data } = await api.post("/auth/login", { email, password });
    return data;
  },

  async logout() {
    const { data } = await api.post("/auth/logout");
    return data;
  },

  async getCurrentUser() {
    const { data } = await api.get("/auth/me");
    return data;
  },

  async updateProfile({ fullName }) {
    const { data } = await api.put("/users/profile", { fullName });
    return data;
  },

  async updatePassword({ currentPassword, newPassword, confirmNewPassword }) {
    const { data } = await api.put("/users/password", {
      currentPassword,
      newPassword,
      confirmNewPassword,
    });
    return data;
  },
};

export default authService;

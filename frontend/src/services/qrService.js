import api from "./api";

const qrService = {
  async generate(payload) {
    const { data } = await api.post("/qrcodes", payload);
    return data;
  },

  async getHistory() {
    const { data } = await api.get("/qrcodes");
    return data;
  },

  async getById(id) {
    const { data } = await api.get(`/qrcodes/${id}`);
    return data;
  },

  async remove(id) {
    const { data } = await api.delete(`/qrcodes/${id}`);
    return data;
  },

  async downloadUrl(id) {
    // Returns the backend URL; the actual download is triggered via an <a> tag
    // or window.open so the browser handles the Content-Disposition header.
    return `/api/qrcodes/${id}/download`;
  },

  async downloadBlob(id) {
    const response = await api.get(`/qrcodes/${id}/download`, {
      responseType: "blob",
    });
    return response.data;
  },
};

export default qrService;

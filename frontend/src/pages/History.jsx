import React, { useCallback, useEffect, useState } from "react";
import qrService from "../services/qrService";
import QRHistory from "../components/QRHistory.jsx";
import Loading from "../components/Loading.jsx";

export default function History() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [deletingId, setDeletingId] = useState(null);

  const loadHistory = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const data = await qrService.getHistory();
      setItems(data);
    } catch (err) {
      setError(err.message || "Failed to load QR code history.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadHistory();
  }, [loadHistory]);

  const handleDelete = async (id) => {
    setDeletingId(id);
    setError("");
    try {
      await qrService.remove(id);
      setItems((prev) => prev.filter((item) => item.id !== id));
    } catch (err) {
      setError(err.message || "Failed to delete QR code.");
    } finally {
      setDeletingId(null);
    }
  };

  const handleDownload = (item) => {
    const link = document.createElement("a");
    link.href = item.image;
    link.download = `qrcode-${item.type.toLowerCase()}-${item.id}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="page history-page">
      <div className="page-header">
        <h1>QR Code History</h1>
        <p>All the QR codes you've generated, newest first.</p>
      </div>

      {error && (
        <div className="alert alert-error" role="alert">
          {error}
        </div>
      )}

      {loading ? (
        <Loading label="Loading history..." />
      ) : (
        <QRHistory
          items={items}
          onDelete={handleDelete}
          onDownload={handleDownload}
          deletingId={deletingId}
        />
      )}
    </div>
  );
}

import React, { useState } from "react";
import qrService from "../services/qrService";
import QRForm from "../components/QRForm.jsx";
import QRPreview from "../components/QRPreview.jsx";

export default function Dashboard() {
  const [qrCode, setQrCode] = useState(null);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState("");

  const handleGenerate = async (payload) => {
    setError("");
    setGenerating(true);
    try {
      const result = await qrService.generate(payload);
      setQrCode(result);
    } catch (err) {
      setError(err.message || "Failed to generate QR code. Please try again.");
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="page dashboard-page">
      <div className="page-header">
        <h1>QR Code Generator</h1>
        <p>Choose a type, fill in the details, and generate your QR code instantly.</p>
      </div>

      {error && (
        <div className="alert alert-error" role="alert">
          {error}
        </div>
      )}

      <div className="dashboard-grid">
        <div className="card generator-card">
          <h2 className="card-title">Generator</h2>
          <QRForm onGenerate={handleGenerate} generating={generating} />
        </div>

        <div className="card qr-preview-card">
          <h2 className="card-title">Preview</h2>
          <QRPreview qrCode={qrCode} onGenerateAgain={() => setQrCode(null)} />
        </div>
      </div>
    </div>
  );
}
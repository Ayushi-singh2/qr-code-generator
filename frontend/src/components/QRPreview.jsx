import React from "react";

function formatDate(dateStr) {
  if (!dateStr) return "";
  try {
    return new Date(dateStr).toLocaleString();
  } catch {
    return dateStr;
  }
}

export default function QRPreview({ qrCode, onGenerateAgain }) {
  if (!qrCode) {
    return (
      <div className="qr-preview qr-preview-empty">
        <div className="qr-placeholder" aria-hidden="true">
          <span>▦</span>
        </div>
        <p>Your generated QR code will appear here</p>
      </div>
    );
  }

  const handleDownload = () => {
    const link = document.createElement("a");
    link.href = qrCode.image;
    link.download = `qrcode-${qrCode.type.toLowerCase()}-${qrCode.id}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="qr-preview">
      <img src={qrCode.image} alt={`Generated ${qrCode.type} QR code`} className="qr-image" />
      <div className="qr-meta">
        <span className="badge">{qrCode.type}</span>
        <span className="qr-date">{formatDate(qrCode.createdAt)}</span>
      </div>
      <div className="qr-actions">
        <button className="btn btn-primary" onClick={handleDownload}>
          Download PNG
        </button>
        {onGenerateAgain && (
          <button className="btn btn-secondary" onClick={onGenerateAgain}>
            Generate Again
          </button>
        )}
      </div>
    </div>
  );
}

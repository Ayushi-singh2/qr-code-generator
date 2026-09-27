import React, { useState } from "react";

function formatDate(dateStr) {
  if (!dateStr) return "";
  try {
    return new Date(dateStr).toLocaleString();
  } catch {
    return dateStr;
  }
}

export default function QRHistory({ items, onDelete, onDownload, deletingId }) {
  const [confirmId, setConfirmId] = useState(null);

  if (!items || items.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-icon" aria-hidden="true">▦</div>
        <h3>No QR codes yet</h3>
        <p>QR codes you generate will show up here.</p>
      </div>
    );
  }

  return (
    <div className="qr-history-list">
      {items.map((item) => (
        <div className="qr-history-item" key={item.id}>
          <img src={item.image} alt={`${item.type} QR code`} className="qr-history-thumb" />
          <div className="qr-history-details">
            <div className="qr-history-top">
              <span className="badge">{item.type}</span>
              <span className="qr-date">{formatDate(item.createdAt)}</span>
            </div>
            <p className="qr-history-content" title={item.content}>
              {item.content}
            </p>
          </div>
          <div className="qr-history-actions">
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => onDownload(item)}
              aria-label={`Download QR code from ${formatDate(item.createdAt)}`}
            >
              Download
            </button>
            {confirmId === item.id ? (
              <div className="confirm-inline">
                <span>Delete?</span>
                <button
                  className="btn btn-danger btn-sm"
                  disabled={deletingId === item.id}
                  onClick={() => {
                    onDelete(item.id);
                    setConfirmId(null);
                  }}
                >
                  Yes
                </button>
                <button className="btn btn-secondary btn-sm" onClick={() => setConfirmId(null)}>
                  No
                </button>
              </div>
            ) : (
              <button
                className="btn btn-danger btn-sm"
                onClick={() => setConfirmId(item.id)}
                aria-label={`Delete QR code from ${formatDate(item.createdAt)}`}
              >
                Delete
              </button>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}

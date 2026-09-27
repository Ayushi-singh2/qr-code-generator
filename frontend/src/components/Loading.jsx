import React from "react";

export default function Loading({ label = "Loading..." }) {
  return (
    <div className="loading-container" role="status" aria-live="polite">
      <div className="spinner" />
      <span className="loading-label">{label}</span>
    </div>
  );
}

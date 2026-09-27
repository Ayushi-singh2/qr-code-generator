import React from "react";

export default function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="app-footer">
      <div className="app-footer-inner">
        <div className="footer-brand">
          <span className="brand-icon" aria-hidden="true">
            <span /><span /><span />
            <span /><span /><span />
            <span /><span /><span />
          </span>
          <span>QR Generator</span>
        </div>

        <p className="footer-note">
          Encode text, links, and contact details into scannable QR codes in seconds.
        </p>

        <p className="footer-copy">&copy; {year} QR Generator. All rights reserved.</p>
      </div>
    </footer>
  );
}
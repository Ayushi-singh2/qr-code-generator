import React, { useState } from "react";

const QR_TYPES = [
  { value: "TEXT", label: "Text" },
  { value: "URL", label: "URL" },
  { value: "EMAIL", label: "Email" },
  { value: "PHONE", label: "Phone" },
  { value: "WIFI", label: "Wi-Fi" },
  { value: "VCARD", label: "Contact (vCard)" },
];

const SIZES = [200, 300, 500];

const initialFormState = {
  content: "",
  email: "",
  subject: "",
  message: "",
  phoneNumber: "",
  ssid: "",
  wifiPassword: "",
  encryption: "WPA",
  hidden: false,
  firstName: "",
  lastName: "",
  organization: "",
  vcardPhone: "",
  vcardEmail: "",
  website: "",
  address: "",
};

export default function QRForm({ onGenerate, generating }) {
  const [type, setType] = useState("TEXT");
  const [size, setSize] = useState(300);
  const [fields, setFields] = useState(initialFormState);
  const [errors, setErrors] = useState({});

  const update = (name, value) => {
    setFields((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: undefined }));
  };

  const validate = () => {
    const next = {};
    switch (type) {
      case "TEXT":
        if (!fields.content.trim()) next.content = "Please enter some text";
        break;
      case "URL":
        if (!fields.content.trim()) next.content = "Please enter a URL";
        break;
      case "EMAIL":
        if (!fields.email.trim()) next.email = "Email is required";
        break;
      case "PHONE":
        if (!fields.phoneNumber.trim()) next.phoneNumber = "Phone number is required";
        break;
      case "WIFI":
        if (!fields.ssid.trim()) next.ssid = "SSID is required";
        break;
      case "VCARD":
        if (!fields.firstName.trim() && !fields.lastName.trim()) {
          next.firstName = "First or last name is required";
        }
        break;
      default:
        break;
    }
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validate()) return;

    const payload = { type, size, ...fields };
    onGenerate(payload);
  };

  return (
    <form className="qr-form" onSubmit={handleSubmit} noValidate>
      <div className="form-group">
        <label htmlFor="qr-type">QR Code Type</label>
        <select id="qr-type" value={type} onChange={(e) => setType(e.target.value)}>
          {QR_TYPES.map((t) => (
            <option key={t.value} value={t.value}>
              {t.label}
            </option>
          ))}
        </select>
      </div>

      {type === "TEXT" && (
        <div className="form-group">
          <label htmlFor="content">Text</label>
          <textarea
            id="content"
            rows={4}
            placeholder="Enter any text..."
            value={fields.content}
            onChange={(e) => update("content", e.target.value)}
            aria-invalid={!!errors.content}
          />
          {errors.content && <span className="field-error">{errors.content}</span>}
        </div>
      )}

      {type === "URL" && (
        <div className="form-group">
          <label htmlFor="url-content">Website URL</label>
          <input
            id="url-content"
            type="text"
            placeholder="https://example.com"
            value={fields.content}
            onChange={(e) => update("content", e.target.value)}
            aria-invalid={!!errors.content}
          />
          {errors.content && <span className="field-error">{errors.content}</span>}
        </div>
      )}

      {type === "EMAIL" && (
        <>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              placeholder="someone@example.com"
              value={fields.email}
              onChange={(e) => update("email", e.target.value)}
              aria-invalid={!!errors.email}
            />
            {errors.email && <span className="field-error">{errors.email}</span>}
          </div>
          <div className="form-group">
            <label htmlFor="subject">Subject</label>
            <input
              id="subject"
              type="text"
              placeholder="Subject"
              value={fields.subject}
              onChange={(e) => update("subject", e.target.value)}
            />
          </div>
          <div className="form-group">
            <label htmlFor="message">Message</label>
            <textarea
              id="message"
              rows={3}
              placeholder="Message"
              value={fields.message}
              onChange={(e) => update("message", e.target.value)}
            />
          </div>
        </>
      )}

      {type === "PHONE" && (
        <div className="form-group">
          <label htmlFor="phoneNumber">Phone Number</label>
          <input
            id="phoneNumber"
            type="tel"
            placeholder="+1 555 123 4567"
            value={fields.phoneNumber}
            onChange={(e) => update("phoneNumber", e.target.value)}
            aria-invalid={!!errors.phoneNumber}
          />
          {errors.phoneNumber && <span className="field-error">{errors.phoneNumber}</span>}
        </div>
      )}

      {type === "WIFI" && (
        <>
          <div className="form-group">
            <label htmlFor="ssid">Network Name (SSID)</label>
            <input
              id="ssid"
              type="text"
              placeholder="MyWiFiNetwork"
              value={fields.ssid}
              onChange={(e) => update("ssid", e.target.value)}
              aria-invalid={!!errors.ssid}
            />
            {errors.ssid && <span className="field-error">{errors.ssid}</span>}
          </div>
          <div className="form-group">
            <label htmlFor="wifiPassword">Password</label>
            <input
              id="wifiPassword"
              type="text"
              placeholder="Wi-Fi password"
              value={fields.wifiPassword}
              onChange={(e) => update("wifiPassword", e.target.value)}
              disabled={fields.encryption === "None"}
            />
          </div>
          <div className="form-group">
            <label htmlFor="encryption">Encryption</label>
            <select
              id="encryption"
              value={fields.encryption}
              onChange={(e) => update("encryption", e.target.value)}
            >
              <option value="WPA">WPA/WPA2</option>
              <option value="WEP">WEP</option>
              <option value="None">None</option>
            </select>
          </div>
          <div className="form-group form-checkbox">
            <input
              id="hidden"
              type="checkbox"
              checked={fields.hidden}
              onChange={(e) => update("hidden", e.target.checked)}
            />
            <label htmlFor="hidden">Hidden network</label>
          </div>
        </>
      )}

      {type === "VCARD" && (
        <>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="firstName">First Name</label>
              <input
                id="firstName"
                type="text"
                value={fields.firstName}
                onChange={(e) => update("firstName", e.target.value)}
                aria-invalid={!!errors.firstName}
              />
              {errors.firstName && <span className="field-error">{errors.firstName}</span>}
            </div>
            <div className="form-group">
              <label htmlFor="lastName">Last Name</label>
              <input
                id="lastName"
                type="text"
                value={fields.lastName}
                onChange={(e) => update("lastName", e.target.value)}
              />
            </div>
          </div>
          <div className="form-group">
            <label htmlFor="organization">Organization</label>
            <input
              id="organization"
              type="text"
              value={fields.organization}
              onChange={(e) => update("organization", e.target.value)}
            />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="vcardPhone">Phone</label>
              <input
                id="vcardPhone"
                type="tel"
                value={fields.vcardPhone}
                onChange={(e) => update("vcardPhone", e.target.value)}
              />
            </div>
            <div className="form-group">
              <label htmlFor="vcardEmail">Email</label>
              <input
                id="vcardEmail"
                type="email"
                value={fields.vcardEmail}
                onChange={(e) => update("vcardEmail", e.target.value)}
              />
            </div>
          </div>
          <div className="form-group">
            <label htmlFor="website">Website</label>
            <input
              id="website"
              type="text"
              value={fields.website}
              onChange={(e) => update("website", e.target.value)}
            />
          </div>
          <div className="form-group">
            <label htmlFor="address">Address</label>
            <input
              id="address"
              type="text"
              value={fields.address}
              onChange={(e) => update("address", e.target.value)}
            />
          </div>
        </>
      )}

      <div className="form-group">
        <label htmlFor="size">QR Code Size</label>
        <select id="size" value={size} onChange={(e) => setSize(Number(e.target.value))}>
          {SIZES.map((s) => (
            <option key={s} value={s}>
              {s} x {s}
            </option>
          ))}
        </select>
      </div>

      <button type="submit" className="btn btn-primary btn-block" disabled={generating}>
        {generating ? "Generating..." : "Generate QR Code"}
      </button>
    </form>
  );
}

import React, { useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import authService from "../services/authService";

function formatDate(dateStr) {
  if (!dateStr) return "";
  try {
    return new Date(dateStr).toLocaleDateString(undefined, {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  } catch {
    return dateStr;
  }
}

export default function Profile() {
  const { user, updateUser } = useAuth();

  const [fullName, setFullName] = useState(user?.fullName || "");
  const [profileError, setProfileError] = useState("");
  const [profileSuccess, setProfileSuccess] = useState("");
  const [savingProfile, setSavingProfile] = useState(false);

  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmNewPassword: "",
  });
  const [passwordErrors, setPasswordErrors] = useState({});
  const [passwordError, setPasswordError] = useState("");
  const [passwordSuccess, setPasswordSuccess] = useState("");
  const [savingPassword, setSavingPassword] = useState(false);

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setProfileError("");
    setProfileSuccess("");
    if (!fullName.trim()) {
      setProfileError("Full name is required");
      return;
    }
    setSavingProfile(true);
    try {
      const updated = await authService.updateProfile({ fullName: fullName.trim() });
      updateUser(updated);
      setProfileSuccess("Profile updated successfully");
    } catch (err) {
      setProfileError(err.message || "Failed to update profile");
    } finally {
      setSavingProfile(false);
    }
  };

  const updatePasswordField = (name, value) => {
    setPasswordForm((prev) => ({ ...prev, [name]: value }));
    setPasswordErrors((prev) => ({ ...prev, [name]: undefined }));
  };

  const validatePasswordForm = () => {
    const next = {};
    if (!passwordForm.currentPassword) next.currentPassword = "Current password is required";
    if (!passwordForm.newPassword) next.newPassword = "New password is required";
    else if (passwordForm.newPassword.length < 8) next.newPassword = "Must be at least 8 characters";
    if (!passwordForm.confirmNewPassword) next.confirmNewPassword = "Please confirm your new password";
    else if (passwordForm.newPassword !== passwordForm.confirmNewPassword)
      next.confirmNewPassword = "Passwords do not match";
    setPasswordErrors(next);
    return Object.keys(next).length === 0;
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setPasswordError("");
    setPasswordSuccess("");
    if (!validatePasswordForm()) return;

    setSavingPassword(true);
    try {
      await authService.updatePassword(passwordForm);
      setPasswordSuccess("Password updated successfully");
      setPasswordForm({ currentPassword: "", newPassword: "", confirmNewPassword: "" });
    } catch (err) {
      setPasswordError(err.message || "Failed to update password");
    } finally {
      setSavingPassword(false);
    }
  };

  return (
    <div className="page profile-page">
      <div className="page-header">
        <h1>Profile</h1>
        <p>Manage your account details and password.</p>
      </div>

      <div className="profile-grid">
        <div className="card">
          <h2 className="card-title">Account Details</h2>
          <div className="profile-static">
            <div>
              <span className="profile-label">Email</span>
              <span>{user?.email}</span>
            </div>
            <div>
              <span className="profile-label">Member Since</span>
              <span>{formatDate(user?.createdAt)}</span>
            </div>
          </div>

          {profileError && (
            <div className="alert alert-error" role="alert">
              {profileError}
            </div>
          )}
          {profileSuccess && (
            <div className="alert alert-success" role="status">
              {profileSuccess}
            </div>
          )}

          <form onSubmit={handleProfileSubmit} noValidate>
            <div className="form-group">
              <label htmlFor="fullName">Full Name</label>
              <input
                id="fullName"
                type="text"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
              />
            </div>
            <button type="submit" className="btn btn-primary" disabled={savingProfile}>
              {savingProfile ? "Saving..." : "Save Changes"}
            </button>
          </form>
        </div>

        <div className="card">
          <h2 className="card-title">Change Password</h2>

          {passwordError && (
            <div className="alert alert-error" role="alert">
              {passwordError}
            </div>
          )}
          {passwordSuccess && (
            <div className="alert alert-success" role="status">
              {passwordSuccess}
            </div>
          )}

          <form onSubmit={handlePasswordSubmit} noValidate>
            <div className="form-group">
              <label htmlFor="currentPassword">Current Password</label>
              <input
                id="currentPassword"
                type="password"
                autoComplete="current-password"
                value={passwordForm.currentPassword}
                onChange={(e) => updatePasswordField("currentPassword", e.target.value)}
                aria-invalid={!!passwordErrors.currentPassword}
              />
              {passwordErrors.currentPassword && (
                <span className="field-error">{passwordErrors.currentPassword}</span>
              )}
            </div>
            <div className="form-group">
              <label htmlFor="newPassword">New Password</label>
              <input
                id="newPassword"
                type="password"
                autoComplete="new-password"
                value={passwordForm.newPassword}
                onChange={(e) => updatePasswordField("newPassword", e.target.value)}
                aria-invalid={!!passwordErrors.newPassword}
              />
              {passwordErrors.newPassword && (
                <span className="field-error">{passwordErrors.newPassword}</span>
              )}
            </div>
            <div className="form-group">
              <label htmlFor="confirmNewPassword">Confirm New Password</label>
              <input
                id="confirmNewPassword"
                type="password"
                autoComplete="new-password"
                value={passwordForm.confirmNewPassword}
                onChange={(e) => updatePasswordField("confirmNewPassword", e.target.value)}
                aria-invalid={!!passwordErrors.confirmNewPassword}
              />
              {passwordErrors.confirmNewPassword && (
                <span className="field-error">{passwordErrors.confirmNewPassword}</span>
              )}
            </div>
            <button type="submit" className="btn btn-primary" disabled={savingPassword}>
              {savingPassword ? "Updating..." : "Update Password"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

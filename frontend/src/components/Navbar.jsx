import React, { useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="navbar-brand" onClick={() => setMenuOpen(false)}>
          <span className="brand-icon" aria-hidden="true">
            <span /><span /><span />
            <span /><span /><span />
            <span /><span /><span />
          </span>
          QR Generator
        </Link>

        <button
          className="navbar-toggle"
          aria-label="Toggle navigation menu"
          aria-expanded={menuOpen}
          onClick={() => setMenuOpen((v) => !v)}
        >
          <span />
          <span />
          <span />
        </button>

        <nav className={`navbar-links ${menuOpen ? "open" : ""}`}>
          {isAuthenticated ? (
            <>
              <NavLink to="/dashboard" onClick={() => setMenuOpen(false)}>
                Dashboard
              </NavLink>
              <NavLink to="/history" onClick={() => setMenuOpen(false)}>
                History
              </NavLink>
              <NavLink to="/profile" onClick={() => setMenuOpen(false)}>
                Profile
              </NavLink>
              <span className="navbar-user">Hi, {user?.fullName?.split(" ")[0]}</span>
              <button className="btn btn-secondary btn-sm" onClick={handleLogout}>
                Logout
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" onClick={() => setMenuOpen(false)}>
                Login
              </NavLink>
              <NavLink to="/register" onClick={() => setMenuOpen(false)}>
                Register
              </NavLink>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
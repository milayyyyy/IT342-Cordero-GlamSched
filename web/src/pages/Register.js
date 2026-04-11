import React, { useState } from 'react';
import { apiPost } from '../utils/api';
import { useNavigate, Link } from 'react-router-dom';

const ToastSuccess = ({ message }) => (
  <div className="toast">
    <span className="toast-icon">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="20 6 9 17 4 12"/>
      </svg>
    </span>
    {message}
  </div>
);

const IconUser = () => (
  <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="#a09080" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
    <circle cx="12" cy="7" r="4"/>
  </svg>
);

const IconEmail = () => (
  <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="#a09080" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="2" y="4" width="20" height="16" rx="2"/>
    <path d="M2 7l10 7 10-7"/>
  </svg>
);

const IconLock = () => (
  <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="#a09080" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="11" width="18" height="11" rx="2"/>
    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
  </svg>
);

const IconEye = () => (
  <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
    <circle cx="12" cy="12" r="3"/>
  </svg>
);

const IconEyeOff = () => (
  <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
    <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
    <line x1="1" y1="1" x2="23" y2="23"/>
  </svg>
);

function Register() {
  const navigate = useNavigate();
  const [role, setRole] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [toast, setToast] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const data = await apiPost('/auth/register', {
        fullName: formData.name,
        email: formData.email,
        password: formData.password,
        role: role
      });
      if (data.success) {
        setToast(true);
        setTimeout(() => navigate('/login'), 2000);
      } else {
        setError(data.error || 'Registration failed');
      }
    } catch (err) {
      setError(err.message || 'Connection error. Is the backend running?');
    } finally {
      setLoading(false);
    }
  };

  if (!role) {
    return (
      <div className="page-wrapper">
        <div className="auth-card">
          <div className="auth-header">
            <h1>GlamSched</h1>
            <p className="subtitle">Join the Beauty Community</p>
          </div>
          <div className="auth-body">
            <h2>I am a…</h2>
            <div className="role-cards">
              <div className="role-card" onClick={() => setRole('CLIENT')}>
                <span className="role-icon">💅</span>
                <h3>Client</h3>
                <p>Discover &amp; book beauty services</p>
              </div>
              <div className="role-card" onClick={() => setRole('ARTIST')}>
                <span className="role-icon">🎨</span>
                <h3>Artist</h3>
                <p>Showcase &amp; manage your services</p>
              </div>
            </div>
            <div className="auth-footer">
              Already have an account? <Link to="/login">Sign in</Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-wrapper">
      {toast && <ToastSuccess message="Account created! Redirecting to login…" />}
      <div className="auth-card">
        <div className="auth-header">
          <h1>GlamSched</h1>
          <p className="subtitle">Create your account</p>
        </div>
        <div className="auth-body">
          <div className="role-badge">{role === 'CLIENT' ? '💅 Client' : '🎨 Artist'}</div>
          <form onSubmit={handleRegister}>
            <div className="input-group">
              <span className="input-icon"><IconUser /></span>
              <input
                name="name"
                placeholder="Full Name"
                value={formData.name}
                onChange={handleChange}
                required
              />
            </div>
            <div className="input-group">
              <span className="input-icon"><IconEmail /></span>
              <input
                name="email"
                type="email"
                placeholder="Email address"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>
            <div className="input-group">
              <span className="input-icon"><IconLock /></span>
              <input
                name="password"
                type={showPassword ? 'text' : 'password'}
                placeholder="Password"
                value={formData.password}
                onChange={handleChange}
                required
              />
              <button type="button" className="toggle-password" onClick={() => setShowPassword(v => !v)}>
                {showPassword ? <IconEyeOff /> : <IconEye />}
              </button>
            </div>
            <div className="input-group">
              <span className="input-icon"><IconLock /></span>
              <input
                name="confirmPassword"
                type={showConfirm ? 'text' : 'password'}
                placeholder="Confirm Password"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
              />
              <button type="button" className="toggle-password" onClick={() => setShowConfirm(v => !v)}>
                {showConfirm ? <IconEyeOff /> : <IconEye />}
              </button>
            </div>
            {error && <div className="error-msg">{error}</div>}
            <button className="btn-primary" type="submit" disabled={loading}>
              {loading ? 'Creating account…' : 'Create Account'}
            </button>
          </form>
          <button className="btn-secondary" onClick={() => setRole(null)}>Change Role</button>
        </div>
      </div>
    </div>
  );
}

export default Register;

import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { LogIn, Key, Mail, ShieldAlert, Sparkles } from 'lucide-react';
import Toast from '../../components/common/Toast';

export const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [loading, setLoading] = useState(false);

  const validate = () => {
    const errs = {};
    if (!email.trim()) {
      errs.email = 'Email address is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      errs.email = 'Please enter a valid email address';
    }

    if (!password) {
      errs.password = 'Password is required';
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;

    setLoading(true);
    try {
      const user = await login(email.trim(), password);
      const from = location.state?.from?.pathname || (user.role === 'STAFF' ? '/staff/rooms' : '/customer/booking');
      navigate(from, { replace: true });
    } catch (err) {
      const msg = err.response?.data?.message || 'Login failed. Please check your credentials.';
      setServerError(msg);
    } finally {
      setLoading(false);
    }
  };

  const fillStaffCreds = () => {
    setEmail('staff@fuminihotel.com');
    setPassword('Staff@123');
    setErrors({});
    setServerError('');
  };

  const fillCustomerCreds = () => {
    setEmail('customer1@hotel.com');
    setPassword('Customer@123');
    setErrors({});
    setServerError('');
  };

  return (
    <div className="auth-page-container">
      <div className="auth-card">
        <div className="auth-header">
          <div className="auth-icon-badge">
            <LogIn size={28} />
          </div>
          <h2>Welcome Back</h2>
          <p>Sign in to your FU Mini Hotel account</p>
        </div>

        {serverError && <Toast message={serverError} type="error" onClose={() => setServerError('')} />}

        <form onSubmit={handleSubmit} noValidate className="auth-form">
          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <div className="input-with-icon">
              <Mail size={18} className="input-icon" />
              <input
                id="email"
                type="email"
                name="email"
                placeholder="name@example.com"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  if (errors.email) setErrors((prev) => ({ ...prev, email: '' }));
                }}
                className={errors.email ? 'input-error' : ''}
              />
            </div>
            {errors.email && <span className="field-error">{errors.email}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <div className="input-with-icon">
              <Key size={18} className="input-icon" />
              <input
                id="password"
                type="password"
                name="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  if (errors.password) setErrors((prev) => ({ ...prev, password: '' }));
                }}
                className={errors.password ? 'input-error' : ''}
              />
            </div>
            {errors.password && <span className="field-error">{errors.password}</span>}
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>

        <div className="demo-credentials-section">
          <div className="demo-title">
            <Sparkles size={16} />
            <span>Quick Demo Logins</span>
          </div>
          <div className="demo-buttons">
            <button type="button" onClick={fillStaffCreds} className="btn-demo btn-demo-staff">
              Fill Staff Credentials
            </button>
            <button type="button" onClick={fillCustomerCreds} className="btn-demo btn-demo-customer">
              Fill Customer Credentials
            </button>
          </div>
        </div>

        <div className="auth-footer">
          <p>
            Don't have an account? <Link to="/register">Register here</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;

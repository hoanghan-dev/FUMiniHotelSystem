import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { UserPlus, User, Phone, Mail, Calendar, Key } from 'lucide-react';
import Toast from '../../components/common/Toast';

export const RegisterPage = () => {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    customerFullName: '',
    telephone: '',
    emailAddress: '',
    customerBirthday: '',
    password: '',
    confirmPassword: '',
  });

  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [loading, setLoading] = useState(false);

  const validate = () => {
    const errs = {};
    if (!formData.customerFullName.trim()) {
      errs.customerFullName = 'Full name is required';
    } else if (formData.customerFullName.length > 100) {
      errs.customerFullName = 'Full name cannot exceed 100 characters';
    }

    if (!formData.telephone.trim()) {
      errs.telephone = 'Telephone number is required';
    } else if (!/^[0-9]{10,12}$/.test(formData.telephone.trim())) {
      errs.telephone = 'Telephone must be between 10 and 12 digits';
    }

    if (!formData.emailAddress.trim()) {
      errs.emailAddress = 'Email address is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.emailAddress.trim())) {
      errs.emailAddress = 'Please enter a valid email address';
    }

    if (!formData.customerBirthday) {
      errs.customerBirthday = 'Birthday is required';
    } else {
      const birthDate = new Date(formData.customerBirthday);
      const today = new Date();
      if (birthDate >= today) {
        errs.customerBirthday = 'Birthday must be a date in the past';
      }
    }

    if (!formData.password) {
      errs.password = 'Password is required';
    } else if (formData.password.length < 6) {
      errs.password = 'Password must be at least 6 characters';
    }

    if (formData.password !== formData.confirmPassword) {
      errs.confirmPassword = 'Passwords do not match';
    }

    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    setSuccessMsg('');
    if (!validate()) return;

    setLoading(true);
    try {
      await register({
        customerFullName: formData.customerFullName.trim(),
        telephone: formData.telephone.trim(),
        emailAddress: formData.emailAddress.trim(),
        customerBirthday: formData.customerBirthday,
        password: formData.password,
      });

      setSuccessMsg('Account registered successfully! Redirecting to login...');
      setTimeout(() => {
        navigate('/login');
      }, 1500);
    } catch (err) {
      const msg = err.response?.data?.message || 'Registration failed. Please check your data.';
      setServerError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page-container">
      <div className="auth-card auth-card-wide">
        <div className="auth-header">
          <div className="auth-icon-badge">
            <UserPlus size={28} />
          </div>
          <h2>Create Customer Account</h2>
          <p>Join FU Mini Hotel to book rooms and manage your reservations</p>
        </div>

        {serverError && <Toast message={serverError} type="error" onClose={() => setServerError('')} />}
        {successMsg && <Toast message={successMsg} type="success" />}

        <form onSubmit={handleSubmit} noValidate className="auth-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="customerFullName">Full Name</label>
              <div className="input-with-icon">
                <User size={18} className="input-icon" />
                <input
                  id="customerFullName"
                  type="text"
                  name="customerFullName"
                  placeholder="e.g. John Doe"
                  value={formData.customerFullName}
                  onChange={handleChange}
                  className={errors.customerFullName ? 'input-error' : ''}
                />
              </div>
              {errors.customerFullName && <span className="field-error">{errors.customerFullName}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="telephone">Telephone Number</label>
              <div className="input-with-icon">
                <Phone size={18} className="input-icon" />
                <input
                  id="telephone"
                  type="tel"
                  name="telephone"
                  placeholder="e.g. 0901234567"
                  value={formData.telephone}
                  onChange={handleChange}
                  className={errors.telephone ? 'input-error' : ''}
                />
              </div>
              {errors.telephone && <span className="field-error">{errors.telephone}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="emailAddress">Email Address</label>
              <div className="input-with-icon">
                <Mail size={18} className="input-icon" />
                <input
                  id="emailAddress"
                  type="email"
                  name="emailAddress"
                  placeholder="name@example.com"
                  value={formData.emailAddress}
                  onChange={handleChange}
                  className={errors.emailAddress ? 'input-error' : ''}
                />
              </div>
              {errors.emailAddress && <span className="field-error">{errors.emailAddress}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="customerBirthday">Date of Birth</label>
              <div className="input-with-icon">
                <Calendar size={18} className="input-icon" />
                <input
                  id="customerBirthday"
                  type="date"
                  name="customerBirthday"
                  value={formData.customerBirthday}
                  onChange={handleChange}
                  className={errors.customerBirthday ? 'input-error' : ''}
                />
              </div>
              {errors.customerBirthday && <span className="field-error">{errors.customerBirthday}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="password">Password (min 6 chars)</label>
              <div className="input-with-icon">
                <Key size={18} className="input-icon" />
                <input
                  id="password"
                  type="password"
                  name="password"
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleChange}
                  className={errors.password ? 'input-error' : ''}
                />
              </div>
              {errors.password && <span className="field-error">{errors.password}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <div className="input-with-icon">
                <Key size={18} className="input-icon" />
                <input
                  id="confirmPassword"
                  type="password"
                  name="confirmPassword"
                  placeholder="••••••••"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  className={errors.confirmPassword ? 'input-error' : ''}
                />
              </div>
              {errors.confirmPassword && <span className="field-error">{errors.confirmPassword}</span>}
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Creating Account...' : 'Complete Registration'}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            Already have an account? <Link to="/login">Sign in here</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;

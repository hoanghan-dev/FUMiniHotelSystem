import React, { useState, useEffect } from 'react';
import { customerService } from '../../services/customerService';
import { useAuth } from '../../context/AuthContext';
import { User, Phone, Mail, Calendar, Key, Save, CheckCircle } from 'lucide-react';
import Toast from '../../components/common/Toast';

export const CustomerProfilePage = () => {
  const { updateUser } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);

  const [formData, setFormData] = useState({
    customerFullName: '',
    telephone: '',
    emailAddress: '',
    customerBirthday: '',
    password: '',
    confirmPassword: '',
  });

  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    setLoading(true);
    try {
      const data = await customerService.getMyProfile();
      setProfile(data);
      setFormData({
        customerFullName: data.customerFullName || '',
        telephone: data.telephone || '',
        emailAddress: data.emailAddress || '',
        customerBirthday: data.customerBirthday || '',
        password: '',
        confirmPassword: '',
      });
    } catch (err) {
      setToast({ type: 'error', message: 'Failed to load profile details.' });
    } finally {
      setLoading(false);
    }
  };

  const validate = () => {
    const errs = {};
    if (!formData.customerFullName.trim()) errs.customerFullName = 'Full name is required';
    if (!formData.telephone.trim()) {
      errs.telephone = 'Telephone is required';
    } else if (!/^[0-9]{10,12}$/.test(formData.telephone.trim())) {
      errs.telephone = 'Telephone must be 10-12 digits';
    }

    if (!formData.emailAddress.trim()) {
      errs.emailAddress = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.emailAddress.trim())) {
      errs.emailAddress = 'Invalid email address';
    }

    if (!formData.customerBirthday) {
      errs.customerBirthday = 'Birthday is required';
    } else {
      const bDate = new Date(formData.customerBirthday);
      if (bDate >= new Date()) {
        errs.customerBirthday = 'Birthday must be in the past';
      }
    }

    if (formData.password) {
      if (formData.password.length < 6) {
        errs.password = 'New password must be at least 6 characters';
      }
      if (formData.password !== formData.confirmPassword) {
        errs.confirmPassword = 'Passwords do not match';
      }
    }

    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setSaving(true);
    try {
      const payload = {
        customerFullName: formData.customerFullName.trim(),
        telephone: formData.telephone.trim(),
        emailAddress: formData.emailAddress.trim(),
        customerBirthday: formData.customerBirthday,
      };

      if (formData.password) {
        payload.password = formData.password;
      }

      const updated = await customerService.updateMyProfile(payload);
      setProfile(updated);
      updateUser({ fullName: updated.customerFullName, email: updated.emailAddress });
      setToast({ type: 'success', message: 'Profile updated successfully!' });
      setFormData((prev) => ({ ...prev, password: '', confirmPassword: '' }));
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to update profile.';
      setToast({ type: 'error', message: msg });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="page-container loading-state">Loading your profile...</div>;
  }

  return (
    <div className="page-container">
      <div className="page-hero">
        <div className="hero-content">
          <h1>My Profile</h1>
          <p>Manage your personal details, contact information, and security credentials</p>
        </div>
      </div>

      <div className="page-content" style={{ maxWidth: '800px', margin: '0 auto' }}>
        {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}

        <div className="profile-card">
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="profileName">Full Name *</label>
                <div className="input-with-icon">
                  <User size={18} className="input-icon" />
                  <input
                    id="profileName"
                    type="text"
                    value={formData.customerFullName}
                    onChange={(e) => setFormData({ ...formData, customerFullName: e.target.value })}
                    className={errors.customerFullName ? 'input-error' : ''}
                  />
                </div>
                {errors.customerFullName && <span className="field-error">{errors.customerFullName}</span>}
              </div>

              <div className="form-group">
                <label htmlFor="profilePhone">Telephone Number *</label>
                <div className="input-with-icon">
                  <Phone size={18} className="input-icon" />
                  <input
                    id="profilePhone"
                    type="tel"
                    value={formData.telephone}
                    onChange={(e) => setFormData({ ...formData, telephone: e.target.value })}
                    className={errors.telephone ? 'input-error' : ''}
                  />
                </div>
                {errors.telephone && <span className="field-error">{errors.telephone}</span>}
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="profileEmail">Email Address *</label>
                <div className="input-with-icon">
                  <Mail size={18} className="input-icon" />
                  <input
                    id="profileEmail"
                    type="email"
                    value={formData.emailAddress}
                    onChange={(e) => setFormData({ ...formData, emailAddress: e.target.value })}
                    className={errors.emailAddress ? 'input-error' : ''}
                  />
                </div>
                {errors.emailAddress && <span className="field-error">{errors.emailAddress}</span>}
              </div>

              <div className="form-group">
                <label htmlFor="profileBirthday">Birthday *</label>
                <div className="input-with-icon">
                  <Calendar size={18} className="input-icon" />
                  <input
                    id="profileBirthday"
                    type="date"
                    value={formData.customerBirthday}
                    onChange={(e) => setFormData({ ...formData, customerBirthday: e.target.value })}
                    className={errors.customerBirthday ? 'input-error' : ''}
                  />
                </div>
                {errors.customerBirthday && <span className="field-error">{errors.customerBirthday}</span>}
              </div>
            </div>

            <div className="security-section-divider">
              <h4>Change Password (Optional)</h4>
              <p>Leave blank to keep your current password</p>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="profileNewPass">New Password</label>
                <div className="input-with-icon">
                  <Key size={18} className="input-icon" />
                  <input
                    id="profileNewPass"
                    type="password"
                    placeholder="New password (min 6 chars)"
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    className={errors.password ? 'input-error' : ''}
                  />
                </div>
                {errors.password && <span className="field-error">{errors.password}</span>}
              </div>

              <div className="form-group">
                <label htmlFor="profileConfirmPass">Confirm New Password</label>
                <div className="input-with-icon">
                  <Key size={18} className="input-icon" />
                  <input
                    id="profileConfirmPass"
                    type="password"
                    placeholder="Confirm new password"
                    value={formData.confirmPassword}
                    onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                    className={errors.confirmPassword ? 'input-error' : ''}
                  />
                </div>
                {errors.confirmPassword && <span className="field-error">{errors.confirmPassword}</span>}
              </div>
            </div>

            <div style={{ marginTop: '24px' }}>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                <Save size={18} />
                <span>{saving ? 'Saving Changes...' : 'Save Profile Changes'}</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CustomerProfilePage;

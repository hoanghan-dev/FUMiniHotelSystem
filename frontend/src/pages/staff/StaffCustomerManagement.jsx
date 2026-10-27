import React, { useState, useEffect } from 'react';
import { customerService } from '../../services/customerService';
import { Plus, Edit2, Trash2, Search, Filter, AlertTriangle, Users } from 'lucide-react';
import Modal from '../../components/common/Modal';
import Toast from '../../components/common/Toast';

export const StaffCustomerManagement = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);

  // Search/Filters
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');

  // Modals
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState(null);
  const [deletingCustomer, setDeletingCustomer] = useState(null);

  // Form
  const [formData, setFormData] = useState({
    customerFullName: '',
    telephone: '',
    emailAddress: '',
    customerBirthday: '',
    customerStatus: '1',
    password: '',
  });
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadCustomers();
  }, []);

  const loadCustomers = async () => {
    setLoading(true);
    try {
      const params = {};
      if (keyword.trim()) params.keyword = keyword.trim();
      if (status) params.status = status;

      const data = await customerService.getCustomers(params);
      setCustomers(data);
    } catch (err) {
      setToast({ type: 'error', message: 'Failed to load customers.' });
    } finally {
      setLoading(false);
    }
  };

  const openCreateModal = () => {
    setEditingCustomer(null);
    setFormData({
      customerFullName: '',
      telephone: '',
      emailAddress: '',
      customerBirthday: '',
      customerStatus: '1',
      password: '',
    });
    setFormErrors({});
    setIsFormModalOpen(true);
  };

  const openEditModal = (c) => {
    setEditingCustomer(c);
    setFormData({
      customerFullName: c.customerFullName,
      telephone: c.telephone || '',
      emailAddress: c.emailAddress,
      customerBirthday: c.customerBirthday || '',
      customerStatus: c.customerStatus || '1',
      password: '',
    });
    setFormErrors({});
    setIsFormModalOpen(true);
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

    if (!editingCustomer && (!formData.password || formData.password.length < 6)) {
      errs.password = 'Password must be at least 6 characters';
    }

    setFormErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setSubmitting(true);
    try {
      if (editingCustomer) {
        const payload = {
          customerFullName: formData.customerFullName.trim(),
          telephone: formData.telephone.trim(),
          emailAddress: formData.emailAddress.trim(),
          customerBirthday: formData.customerBirthday,
          customerStatus: formData.customerStatus,
        };
        if (formData.password && formData.password.trim()) {
          payload.password = formData.password.trim();
        }
        await customerService.updateCustomer(editingCustomer.customerID, payload);
        setToast({ type: 'success', message: 'Customer profile updated successfully!' });
      } else {
        await customerService.createCustomer({
          customerFullName: formData.customerFullName.trim(),
          telephone: formData.telephone.trim(),
          emailAddress: formData.emailAddress.trim(),
          customerBirthday: formData.customerBirthday,
          password: formData.password,
        });
        setToast({ type: 'success', message: 'Customer created successfully!' });
      }

      setIsFormModalOpen(false);
      loadCustomers();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to save customer.';
      setToast({ type: 'error', message: msg });
    } finally {
      setSubmitting(false);
    }
  };

  const openDeleteModal = (c) => {
    setDeletingCustomer(c);
    setIsDeleteModalOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!deletingCustomer) return;
    try {
      const res = await customerService.deleteCustomer(deletingCustomer.customerID);
      const alertType = res.actionTaken === 'DEACTIVATED' ? 'warning' : 'success';
      setToast({ type: alertType, message: res.message });
      setIsDeleteModalOpen(false);
      loadCustomers();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to delete customer.';
      setToast({ type: 'error', message: msg });
    }
  };

  return (
    <div className="admin-page-container">
      <div className="admin-header">
        <div>
          <h2>Customer Management</h2>
          <p>Manage customer profiles, registration records, and account statuses</p>
        </div>
        <button onClick={openCreateModal} className="btn btn-primary">
          <Plus size={18} />
          <span>Add New Customer</span>
        </button>
      </div>

      {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}

      {/* Filter / Search Bar */}
      <div className="filter-card">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            loadCustomers();
          }}
          className="filter-form"
        >
          <div className="filter-group">
            <label htmlFor="searchKeyword">Search Name / Email / Phone</label>
            <input
              id="searchKeyword"
              type="text"
              placeholder="e.g. John or 0901"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <label htmlFor="filterCustStatus">Status</label>
            <select
              id="filterCustStatus"
              value={status}
              onChange={(e) => setStatus(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="1">Active</option>
              <option value="0">Inactive</option>
            </select>
          </div>

          <div className="filter-actions">
            <button type="submit" className="btn btn-primary">
              <Search size={16} />
              <span>Filter</span>
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => {
                setKeyword('');
                setStatus('');
                setTimeout(() => customerService.getCustomers().then(setCustomers), 0);
              }}
            >
              Reset
            </button>
          </div>
        </form>
      </div>

      {/* Table */}
      <div className="table-responsive">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Full Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Birthday</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="7" className="text-center py-4">
                  Loading customers...
                </td>
              </tr>
            ) : customers.length === 0 ? (
              <tr>
                <td colSpan="7" className="text-center py-4">
                  No customers found.
                </td>
              </tr>
            ) : (
              customers.map((c) => {
                const isActive = c.customerStatus === '1' || c.customerStatus === 'Active';
                return (
                  <tr key={c.customerID}>
                    <td>#{c.customerID}</td>
                    <td>
                      <strong>{c.customerFullName}</strong>
                    </td>
                    <td>{c.emailAddress}</td>
                    <td>{c.telephone}</td>
                    <td>{c.customerBirthday}</td>
                    <td>
                      <span className={`status-pill ${isActive ? 'status-active' : 'status-inactive'}`}>
                        {isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button
                          onClick={() => openEditModal(c)}
                          className="btn-icon edit"
                          title="Edit Customer"
                        >
                          <Edit2 size={16} />
                        </button>
                        <button
                          onClick={() => openDeleteModal(c)}
                          className="btn-icon delete"
                          title="Delete / Deactivate Customer"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Create / Edit Modal */}
      <Modal
        isOpen={isFormModalOpen}
        onClose={() => setIsFormModalOpen(false)}
        title={editingCustomer ? `Edit Customer: ${editingCustomer.customerFullName}` : 'Create Customer'}
      >
        <form onSubmit={handleFormSubmit} className="modal-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="modalCustName">Full Name *</label>
              <input
                id="modalCustName"
                type="text"
                value={formData.customerFullName}
                onChange={(e) => setFormData({ ...formData, customerFullName: e.target.value })}
                className={formErrors.customerFullName ? 'input-error' : ''}
              />
              {formErrors.customerFullName && <span className="field-error">{formErrors.customerFullName}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="modalCustPhone">Telephone *</label>
              <input
                id="modalCustPhone"
                type="tel"
                value={formData.telephone}
                onChange={(e) => setFormData({ ...formData, telephone: e.target.value })}
                className={formErrors.telephone ? 'input-error' : ''}
              />
              {formErrors.telephone && <span className="field-error">{formErrors.telephone}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="modalCustEmail">Email Address *</label>
              <input
                id="modalCustEmail"
                type="email"
                value={formData.emailAddress}
                onChange={(e) => setFormData({ ...formData, emailAddress: e.target.value })}
                className={formErrors.emailAddress ? 'input-error' : ''}
              />
              {formErrors.emailAddress && <span className="field-error">{formErrors.emailAddress}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="modalCustBirthday">Date of Birth *</label>
              <input
                id="modalCustBirthday"
                type="date"
                value={formData.customerBirthday}
                onChange={(e) => setFormData({ ...formData, customerBirthday: e.target.value })}
                className={formErrors.customerBirthday ? 'input-error' : ''}
              />
              {formErrors.customerBirthday && <span className="field-error">{formErrors.customerBirthday}</span>}
            </div>
          </div>

          {editingCustomer ? (
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="modalCustStatus">Account Status</label>
                <select
                  id="modalCustStatus"
                  value={formData.customerStatus}
                  onChange={(e) => setFormData({ ...formData, customerStatus: e.target.value })}
                >
                  <option value="1">Active</option>
                  <option value="0">Inactive</option>
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="modalCustPass">Change Password (leave blank to keep current)</label>
                <input
                  id="modalCustPass"
                  type="password"
                  placeholder="New password (optional)"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                />
              </div>
            </div>
          ) : (
            <div className="form-group">
              <label htmlFor="modalCustNewPass">Password (min 6 chars) *</label>
              <input
                id="modalCustNewPass"
                type="password"
                placeholder="••••••••"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                className={formErrors.password ? 'input-error' : ''}
              />
              {formErrors.password && <span className="field-error">{formErrors.password}</span>}
            </div>
          )}

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setIsFormModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : editingCustomer ? 'Update Customer' : 'Create Customer'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete / Deactivate Confirmation Modal */}
      <Modal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        title="Confirm Customer Deletion / Deactivation"
      >
        <div className="confirm-delete-box">
          <AlertTriangle size={36} className="warning-icon" />
          <p>
            Are you sure you want to delete customer <strong>{deletingCustomer?.customerFullName}</strong>?
          </p>
          <div className="delete-policy-note">
            <strong>System Rule:</strong>
            <p>
              If this customer has never made any reservations, their account will be <em>permanently deleted</em>.
              If they have existing booking reservation records, their account status will be set to <em>Inactive</em> to
              maintain transaction integrity.
            </p>
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setIsDeleteModalOpen(false)}>
              Cancel
            </button>
            <button type="button" className="btn btn-danger" onClick={handleDeleteConfirm}>
              Proceed
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default StaffCustomerManagement;

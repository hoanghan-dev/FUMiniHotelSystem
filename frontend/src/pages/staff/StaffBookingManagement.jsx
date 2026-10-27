import React, { useState, useEffect } from 'react';
import { bookingService } from '../../services/bookingService';
import { customerService } from '../../services/customerService';
import { roomService } from '../../services/roomService';
import { Plus, Eye, Trash2, Edit3, Search, Filter, Calendar, BedDouble } from 'lucide-react';
import Modal from '../../components/common/Modal';
import Toast from '../../components/common/Toast';

export const StaffBookingManagement = () => {
  const [bookings, setBookings] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);

  // Filters
  const [filterCustId, setFilterCustId] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');

  // Modals
  const [detailsModalData, setDetailsModalData] = useState(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingBooking, setEditingBooking] = useState(null);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [deletingBookingId, setDeletingBookingId] = useState(null);

  // Create Form State
  const [createCustomerId, setCreateCustomerId] = useState('');
  const [createDetails, setCreateDetails] = useState([
    {
      roomID: '',
      startDate: '',
      endDate: '',
    },
  ]);
  const [createErrors, setCreateErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  // Edit status Form
  const [editStatus, setEditStatus] = useState('1');

  useEffect(() => {
    loadLookups();
    loadBookings();
  }, []);

  const loadLookups = async () => {
    try {
      const [custs, rms] = await Promise.all([customerService.getCustomers(), roomService.getRooms()]);
      setCustomers(custs);
      setRooms(rms.filter((r) => r.roomStatus === '1' || r.roomStatus === 'Active'));
    } catch (err) {
      console.error(err);
    }
  };

  const loadBookings = async () => {
    setLoading(true);
    try {
      const params = {};
      if (filterCustId) params.customerId = parseInt(filterCustId, 10);
      if (filterStatus) params.status = filterStatus;
      if (fromDate) params.fromDate = fromDate;
      if (toDate) params.toDate = toDate;

      const data = await bookingService.getAllBookings(params);
      setBookings(data);
    } catch (err) {
      setToast({ type: 'error', message: 'Failed to load booking reservations.' });
    } finally {
      setLoading(false);
    }
  };

  const addDetailRow = () => {
    setCreateDetails((prev) => [...prev, { roomID: '', startDate: '', endDate: '' }]);
  };

  const removeDetailRow = (index) => {
    if (createDetails.length <= 1) return;
    setCreateDetails((prev) => prev.filter((_, i) => i !== index));
  };

  const updateDetailRow = (index, field, value) => {
    setCreateDetails((prev) => {
      const updated = [...prev];
      updated[index] = { ...updated[index], [field]: value };
      return updated;
    });
  };

  const calculateGrandTotal = () => {
    let total = 0;
    createDetails.forEach((d) => {
      if (d.roomID && d.startDate && d.endDate) {
        const room = rooms.find((r) => r.roomID === parseInt(d.roomID, 10));
        const start = new Date(d.startDate);
        const end = new Date(d.endDate);
        const days = Math.round((end - start) / (1000 * 60 * 60 * 24));
        if (room && days > 0) {
          total += room.roomPricePerDay * days;
        }
      }
    });
    return total;
  };

  const validateCreate = () => {
    const errs = {};
    if (!createCustomerId) errs.customerId = 'Customer is required';

    const detailErrs = [];
    const today = new Date().toISOString().split('T')[0];

    createDetails.forEach((d, idx) => {
      const dErr = {};
      if (!d.roomID) dErr.roomID = 'Select a room';
      if (!d.startDate) {
        dErr.startDate = 'Start date required';
      } else if (d.startDate < today) {
        dErr.startDate = 'Start date cannot be in past';
      }

      if (!d.endDate) {
        dErr.endDate = 'End date required';
      } else if (d.startDate && d.endDate <= d.startDate) {
        dErr.endDate = 'End date must be after start date';
      }

      if (Object.keys(dErr).length > 0) {
        detailErrs[idx] = dErr;
      }
    });

    if (detailErrs.length > 0) errs.details = detailErrs;
    setCreateErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    if (!validateCreate()) return;

    setSubmitting(true);
    try {
      const payload = {
        customerID: parseInt(createCustomerId, 10),
        details: createDetails.map((d) => ({
          roomID: parseInt(d.roomID, 10),
          startDate: d.startDate,
          endDate: d.endDate,
        })),
      };

      await bookingService.createStaffBooking(payload);
      setToast({ type: 'success', message: 'Booking reservation created successfully!' });
      setIsCreateModalOpen(false);
      loadBookings();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to create booking.';
      setToast({ type: 'error', message: msg });
    } finally {
      setSubmitting(false);
    }
  };

  const openEditModal = (b) => {
    setEditingBooking(b);
    setEditStatus(b.bookingStatus || '1');
    setIsEditModalOpen(true);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    if (!editingBooking) return;
    try {
      await bookingService.updateBooking(editingBooking.bookingReservationID, {
        bookingStatus: editStatus,
      });
      setToast({ type: 'success', message: 'Booking status updated successfully!' });
      setIsEditModalOpen(false);
      loadBookings();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to update booking.';
      setToast({ type: 'error', message: msg });
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingBookingId) return;
    try {
      await bookingService.deleteBooking(deletingBookingId);
      setToast({ type: 'success', message: 'Booking reservation deleted successfully.' });
      setIsDeleteModalOpen(false);
      loadBookings();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to delete booking.';
      setToast({ type: 'error', message: msg });
    }
  };

  return (
    <div className="admin-page-container">
      <div className="admin-header">
        <div>
          <h2>Booking Reservations Management</h2>
          <p>Monitor online bookings, manage reservation details, calculate pricing, and update statuses</p>
        </div>
        <button
          onClick={() => {
            setCreateCustomerId(customers.length > 0 ? customers[0].customerID : '');
            setCreateDetails([{ roomID: rooms.length > 0 ? rooms[0].roomID : '', startDate: '', endDate: '' }]);
            setCreateErrors({});
            setIsCreateModalOpen(true);
          }}
          className="btn btn-primary"
        >
          <Plus size={18} />
          <span>New Reservation</span>
        </button>
      </div>

      {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}

      {/* Filter / Search Bar */}
      <div className="filter-card">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            loadBookings();
          }}
          className="filter-form"
        >
          <div className="filter-group">
            <label htmlFor="filterStaffBookingCust">Customer</label>
            <select
              id="filterStaffBookingCust"
              value={filterCustId}
              onChange={(e) => setFilterCustId(e.target.value)}
            >
              <option value="">All Customers</option>
              {customers.map((c) => (
                <option key={c.customerID} value={c.customerID}>
                  {c.customerFullName} ({c.emailAddress})
                </option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <label htmlFor="filterStaffBookingStatus">Status</label>
            <select
              id="filterStaffBookingStatus"
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="1">Confirmed</option>
              <option value="0">Cancelled</option>
            </select>
          </div>

          <div className="filter-group">
            <label htmlFor="filterStaffBookingFrom">From Date</label>
            <input
              id="filterStaffBookingFrom"
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <label htmlFor="filterStaffBookingTo">To Date</label>
            <input
              id="filterStaffBookingTo"
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
            />
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
                setFilterCustId('');
                setFilterStatus('');
                setFromDate('');
                setToDate('');
                setTimeout(() => bookingService.getAllBookings().then(setBookings), 0);
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
              <th>Res #</th>
              <th>Customer</th>
              <th>Booking Date</th>
              <th>Total Price</th>
              <th>Rooms</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="7" className="text-center py-4">
                  Loading reservations...
                </td>
              </tr>
            ) : bookings.length === 0 ? (
              <tr>
                <td colSpan="7" className="text-center py-4">
                  No reservations found.
                </td>
              </tr>
            ) : (
              bookings.map((b) => {
                const isConfirmed = b.bookingStatus === '1' || b.bookingStatus === 'Confirmed';
                return (
                  <tr key={b.bookingReservationID}>
                    <td>#{b.bookingReservationID}</td>
                    <td>
                      <div>
                        <strong>{b.customerFullName}</strong>
                      </div>
                      <small className="text-muted">{b.customerEmail}</small>
                    </td>
                    <td>{b.bookingDate}</td>
                    <td>
                      <strong className="text-success">${parseFloat(b.totalPrice).toFixed(2)}</strong>
                    </td>
                    <td>{b.bookingDetails?.length || 0} room(s)</td>
                    <td>
                      <span className={`status-pill ${isConfirmed ? 'status-active' : 'status-inactive'}`}>
                        {isConfirmed ? 'Confirmed' : 'Cancelled'}
                      </span>
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button
                          onClick={() => setDetailsModalData(b)}
                          className="btn-icon view"
                          title="View Reservation Details"
                        >
                          <Eye size={16} />
                        </button>
                        <button
                          onClick={() => openEditModal(b)}
                          className="btn-icon edit"
                          title="Update Status"
                        >
                          <Edit3 size={16} />
                        </button>
                        <button
                          onClick={() => {
                            setDeletingBookingId(b.bookingReservationID);
                            setIsDeleteModalOpen(true);
                          }}
                          className="btn-icon delete"
                          title="Delete Reservation"
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

      {/* View Details Modal */}
      {detailsModalData && (
        <Modal
          isOpen={!!detailsModalData}
          onClose={() => setDetailsModalData(null)}
          title={`Booking Reservation #${detailsModalData.bookingReservationID}`}
          maxWidth="700px"
        >
          <div className="reservation-modal-content">
            <div className="modal-summary-grid">
              <div>
                <strong>Customer:</strong> {detailsModalData.customerFullName}
              </div>
              <div>
                <strong>Email:</strong> {detailsModalData.customerEmail}
              </div>
              <div>
                <strong>Phone:</strong> {detailsModalData.customerPhone || 'N/A'}
              </div>
              <div>
                <strong>Booking Date:</strong> {detailsModalData.bookingDate}
              </div>
              <div>
                <strong>Status:</strong>{' '}
                <span
                  className={`status-pill ${
                    detailsModalData.bookingStatus === '1' ? 'status-active' : 'status-inactive'
                  }`}
                >
                  {detailsModalData.bookingStatus === '1' ? 'Confirmed' : 'Cancelled'}
                </span>
              </div>
              <div>
                <strong>Grand Total:</strong>{' '}
                <span className="highlight-price">${parseFloat(detailsModalData.totalPrice).toFixed(2)}</span>
              </div>
            </div>

            <h4 style={{ marginTop: '20px', marginBottom: '10px' }}>Booked Rooms Details</h4>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Room</th>
                  <th>Type</th>
                  <th>Check-In</th>
                  <th>Check-Out</th>
                  <th>Duration</th>
                  <th>Actual Price</th>
                </tr>
              </thead>
              <tbody>
                {detailsModalData.bookingDetails?.map((d, i) => (
                  <tr key={i}>
                    <td>
                      <strong>Room {d.roomNumber}</strong>
                    </td>
                    <td>{d.roomTypeName}</td>
                    <td>{d.startDate}</td>
                    <td>{d.endDate}</td>
                    <td>{d.numberOfDays} nights</td>
                    <td>${parseFloat(d.actualPrice).toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="modal-actions" style={{ marginTop: '20px' }}>
              <button type="button" className="btn btn-secondary" onClick={() => setDetailsModalData(null)}>
                Close
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* Create Booking Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Create Booking Reservation"
        maxWidth="750px"
      >
        <form onSubmit={handleCreateSubmit} className="modal-form">
          <div className="form-group">
            <label htmlFor="staffCreateCust">Select Customer *</label>
            <select
              id="staffCreateCust"
              value={createCustomerId}
              onChange={(e) => setCreateCustomerId(e.target.value)}
              className={createErrors.customerId ? 'input-error' : ''}
            >
              <option value="">-- Choose Customer --</option>
              {customers.map((c) => (
                <option key={c.customerID} value={c.customerID}>
                  {c.customerFullName} ({c.emailAddress})
                </option>
              ))}
            </select>
            {createErrors.customerId && <span className="field-error">{createErrors.customerId}</span>}
          </div>

          <h4 style={{ marginTop: '16px', marginBottom: '8px' }}>Rooms to Book</h4>
          {createDetails.map((detail, idx) => (
            <div key={idx} className="room-booking-detail-row">
              <div className="detail-field">
                <label htmlFor={`staffRoomSelect-${idx}`}>Room</label>
                <select
                  id={`staffRoomSelect-${idx}`}
                  value={detail.roomID}
                  onChange={(e) => updateDetailRow(idx, 'roomID', e.target.value)}
                >
                  <option value="">Select Room</option>
                  {rooms.map((r) => (
                    <option key={r.roomID} value={r.roomID}>
                      Room {r.roomNumber} - {r.roomType?.roomTypeName} (${r.roomPricePerDay}/day)
                    </option>
                  ))}
                </select>
                {createErrors.details?.[idx]?.roomID && (
                  <span className="field-error">{createErrors.details[idx].roomID}</span>
                )}
              </div>

              <div className="detail-field">
                <label htmlFor={`staffStartDate-${idx}`}>Check-In</label>
                <input
                  id={`staffStartDate-${idx}`}
                  type="date"
                  value={detail.startDate}
                  onChange={(e) => updateDetailRow(idx, 'startDate', e.target.value)}
                />
                {createErrors.details?.[idx]?.startDate && (
                  <span className="field-error">{createErrors.details[idx].startDate}</span>
                )}
              </div>

              <div className="detail-field">
                <label htmlFor={`staffEndDate-${idx}`}>Check-Out</label>
                <input
                  id={`staffEndDate-${idx}`}
                  type="date"
                  value={detail.endDate}
                  onChange={(e) => updateDetailRow(idx, 'endDate', e.target.value)}
                />
                {createErrors.details?.[idx]?.endDate && (
                  <span className="field-error">{createErrors.details[idx].endDate}</span>
                )}
              </div>

              {createDetails.length > 1 && (
                <button
                  type="button"
                  onClick={() => removeDetailRow(idx)}
                  className="btn-icon delete btn-remove-row"
                  title="Remove this room"
                >
                  <Trash2 size={16} />
                </button>
              )}
            </div>
          ))}

          <button type="button" onClick={addDetailRow} className="btn btn-outline btn-sm" style={{ marginTop: '8px' }}>
            <Plus size={14} />
            <span>Add Another Room</span>
          </button>

          <div className="total-calculation-box">
            <span>Estimated Total:</span>
            <span className="total-sum">${calculateGrandTotal().toFixed(2)}</span>
          </div>

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Creating...' : 'Confirm Reservation'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Edit Booking Modal */}
      <Modal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        title={`Update Reservation #${editingBooking?.bookingReservationID}`}
      >
        <form onSubmit={handleEditSubmit} className="modal-form">
          <div className="form-group">
            <label htmlFor="staffEditStatus">Reservation Status</label>
            <select
              id="staffEditStatus"
              value={editStatus}
              onChange={(e) => setEditStatus(e.target.value)}
            >
              <option value="1">Confirmed (Active)</option>
              <option value="0">Cancelled</option>
            </select>
          </div>

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setIsEditModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Save Status
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        title="Confirm Reservation Deletion"
      >
        <div className="confirm-delete-box">
          <p>Are you sure you want to delete reservation #{deletingBookingId}? This will remove all associated booking details.</p>
          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setIsDeleteModalOpen(false)}>
              Cancel
            </button>
            <button type="button" className="btn btn-danger" onClick={handleDeleteConfirm}>
              Delete
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default StaffBookingManagement;

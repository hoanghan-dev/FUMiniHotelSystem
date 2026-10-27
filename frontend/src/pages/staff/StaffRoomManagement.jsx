import React, { useState, useEffect } from 'react';
import { roomService } from '../../services/roomService';
import { roomTypeService } from '../../services/roomTypeService';
import { Plus, Edit2, Trash2, Search, Filter, AlertTriangle, CheckCircle, BedDouble } from 'lucide-react';
import Modal from '../../components/common/Modal';
import Toast from '../../components/common/Toast';

export const StaffRoomManagement = () => {
  const [rooms, setRooms] = useState([]);
  const [roomTypes, setRoomTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);

  // Search/Filters
  const [searchNumber, setSearchNumber] = useState('');
  const [filterType, setFilterType] = useState('');
  const [filterStatus, setFilterStatus] = useState('');

  // Modals
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingRoom, setEditingRoom] = useState(null);
  const [deletingRoom, setDeletingRoom] = useState(null);

  // Form State
  const [formData, setFormData] = useState({
    roomNumber: '',
    roomDetailDescription: '',
    roomMaxCapacity: 2,
    roomTypeID: '',
    roomStatus: '1',
    roomPricePerDay: '',
  });
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadRoomTypes();
    loadRooms();
  }, []);

  const loadRoomTypes = async () => {
    try {
      const data = await roomTypeService.getRoomTypes();
      setRoomTypes(data);
    } catch (err) {
      console.error(err);
    }
  };

  const loadRooms = async () => {
    setLoading(true);
    try {
      const params = {};
      if (searchNumber.trim()) params.roomNumber = searchNumber.trim();
      if (filterType) params.roomTypeId = parseInt(filterType, 10);
      if (filterStatus) params.roomStatus = filterStatus;

      const data = await roomService.getRooms(params);
      setRooms(data);
    } catch (err) {
      setToast({ type: 'error', message: 'Failed to load rooms.' });
    } finally {
      setLoading(false);
    }
  };

  const openCreateModal = () => {
    setEditingRoom(null);
    setFormData({
      roomNumber: '',
      roomDetailDescription: '',
      roomMaxCapacity: 2,
      roomTypeID: roomTypes.length > 0 ? roomTypes[0].roomTypeID : '',
      roomStatus: '1',
      roomPricePerDay: '',
    });
    setFormErrors({});
    setIsFormModalOpen(true);
  };

  const openEditModal = (room) => {
    setEditingRoom(room);
    setFormData({
      roomNumber: room.roomNumber,
      roomDetailDescription: room.roomDetailDescription || '',
      roomMaxCapacity: room.roomMaxCapacity,
      roomTypeID: room.roomType?.roomTypeID || '',
      roomStatus: room.roomStatus || '1',
      roomPricePerDay: room.roomPricePerDay,
    });
    setFormErrors({});
    setIsFormModalOpen(true);
  };

  const validateForm = () => {
    const errs = {};
    if (!formData.roomNumber.trim()) {
      errs.roomNumber = 'Room number is required';
    }
    if (!formData.roomMaxCapacity || formData.roomMaxCapacity < 1) {
      errs.roomMaxCapacity = 'Capacity must be at least 1';
    }
    if (!formData.roomTypeID) {
      errs.roomTypeID = 'Room type is required';
    }
    if (!formData.roomPricePerDay || parseFloat(formData.roomPricePerDay) <= 0) {
      errs.roomPricePerDay = 'Price per day must be greater than 0';
    }
    setFormErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setSubmitting(true);
    try {
      const payload = {
        roomNumber: formData.roomNumber.trim(),
        roomDetailDescription: formData.roomDetailDescription.trim(),
        roomMaxCapacity: parseInt(formData.roomMaxCapacity, 10),
        roomTypeID: parseInt(formData.roomTypeID, 10),
        roomStatus: formData.roomStatus,
        roomPricePerDay: parseFloat(formData.roomPricePerDay),
      };

      if (editingRoom) {
        await roomService.updateRoom(editingRoom.roomID, payload);
        setToast({ type: 'success', message: `Room ${payload.roomNumber} updated successfully!` });
      } else {
        await roomService.createRoom(payload);
        setToast({ type: 'success', message: `Room ${payload.roomNumber} created successfully!` });
      }

      setIsFormModalOpen(false);
      loadRooms();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to save room.';
      setToast({ type: 'error', message: msg });
    } finally {
      setSubmitting(false);
    }
  };

  const openDeleteModal = (room) => {
    setDeletingRoom(room);
    setIsDeleteModalOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!deletingRoom) return;
    try {
      const res = await roomService.deleteRoom(deletingRoom.roomID);
      // Room delete vs deactivate feedback
      const alertType = res.actionTaken === 'DEACTIVATED' ? 'warning' : 'success';
      setToast({ type: alertType, message: res.message });
      setIsDeleteModalOpen(false);
      loadRooms();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to delete room.';
      setToast({ type: 'error', message: msg });
    }
  };

  return (
    <div className="admin-page-container">
      <div className="admin-header">
        <div>
          <h2>Room Management</h2>
          <p>Create, update, search, and manage hotel room inventory and statuses</p>
        </div>
        <button onClick={openCreateModal} className="btn btn-primary">
          <Plus size={18} />
          <span>Add New Room</span>
        </button>
      </div>

      {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}

      {/* Filter / Search Bar */}
      <div className="filter-card">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            loadRooms();
          }}
          className="filter-form"
        >
          <div className="filter-group">
            <label htmlFor="searchNumber">Search Room Number</label>
            <input
              id="searchNumber"
              type="text"
              placeholder="e.g. 101"
              value={searchNumber}
              onChange={(e) => setSearchNumber(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <label htmlFor="filterType">Room Type</label>
            <select
              id="filterType"
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
            >
              <option value="">All Types</option>
              {roomTypes.map((t) => (
                <option key={t.roomTypeID} value={t.roomTypeID}>
                  {t.roomTypeName}
                </option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <label htmlFor="filterStatus">Status</label>
            <select
              id="filterStatus"
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
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
                setSearchNumber('');
                setFilterType('');
                setFilterStatus('');
                setTimeout(() => roomService.getRooms().then(setRooms), 0);
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
              <th>Room Number</th>
              <th>Room Type</th>
              <th>Capacity</th>
              <th>Price / Day</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="7" className="text-center py-4">
                  Loading rooms...
                </td>
              </tr>
            ) : rooms.length === 0 ? (
              <tr>
                <td colSpan="7" className="text-center py-4">
                  No rooms found.
                </td>
              </tr>
            ) : (
              rooms.map((room) => {
                const isActive = room.roomStatus === '1' || room.roomStatus === 'Active';
                return (
                  <tr key={room.roomID}>
                    <td>#{room.roomID}</td>
                    <td>
                      <strong>{room.roomNumber}</strong>
                    </td>
                    <td>{room.roomType?.roomTypeName || 'N/A'}</td>
                    <td>{room.roomMaxCapacity} guests</td>
                    <td>${parseFloat(room.roomPricePerDay).toFixed(2)}</td>
                    <td>
                      <span className={`status-pill ${isActive ? 'status-active' : 'status-inactive'}`}>
                        {isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button
                          onClick={() => openEditModal(room)}
                          className="btn-icon edit"
                          title="Edit Room"
                        >
                          <Edit2 size={16} />
                        </button>
                        <button
                          onClick={() => openDeleteModal(room)}
                          className="btn-icon delete"
                          title="Delete or Deactivate Room"
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
        title={editingRoom ? `Edit Room ${editingRoom.roomNumber}` : 'Create New Room'}
      >
        <form onSubmit={handleFormSubmit} className="modal-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="modalRoomNumber">Room Number *</label>
              <input
                id="modalRoomNumber"
                type="text"
                value={formData.roomNumber}
                onChange={(e) => setFormData({ ...formData, roomNumber: e.target.value })}
                className={formErrors.roomNumber ? 'input-error' : ''}
              />
              {formErrors.roomNumber && <span className="field-error">{formErrors.roomNumber}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="modalRoomType">Room Type *</label>
              <select
                id="modalRoomType"
                value={formData.roomTypeID}
                onChange={(e) => setFormData({ ...formData, roomTypeID: e.target.value })}
                className={formErrors.roomTypeID ? 'input-error' : ''}
              >
                <option value="">Select Room Type</option>
                {roomTypes.map((t) => (
                  <option key={t.roomTypeID} value={t.roomTypeID}>
                    {t.roomTypeName}
                  </option>
                ))}
              </select>
              {formErrors.roomTypeID && <span className="field-error">{formErrors.roomTypeID}</span>}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="modalMaxCapacity">Max Capacity (Guests) *</label>
              <input
                id="modalMaxCapacity"
                type="number"
                min="1"
                value={formData.roomMaxCapacity}
                onChange={(e) => setFormData({ ...formData, roomMaxCapacity: e.target.value })}
                className={formErrors.roomMaxCapacity ? 'input-error' : ''}
              />
              {formErrors.roomMaxCapacity && <span className="field-error">{formErrors.roomMaxCapacity}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="modalPricePerDay">Price Per Day ($) *</label>
              <input
                id="modalPricePerDay"
                type="number"
                step="0.01"
                min="0.01"
                value={formData.roomPricePerDay}
                onChange={(e) => setFormData({ ...formData, roomPricePerDay: e.target.value })}
                className={formErrors.roomPricePerDay ? 'input-error' : ''}
              />
              {formErrors.roomPricePerDay && <span className="field-error">{formErrors.roomPricePerDay}</span>}
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="modalRoomStatus">Room Status</label>
            <select
              id="modalRoomStatus"
              value={formData.roomStatus}
              onChange={(e) => setFormData({ ...formData, roomStatus: e.target.value })}
            >
              <option value="1">Active</option>
              <option value="0">Inactive</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="modalRoomDesc">Detail Description</label>
            <textarea
              id="modalRoomDesc"
              rows="3"
              value={formData.roomDetailDescription}
              onChange={(e) => setFormData({ ...formData, roomDetailDescription: e.target.value })}
              placeholder="Room features, view, bed configurations..."
            />
          </div>

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setIsFormModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : editingRoom ? 'Update Room' : 'Create Room'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete / Deactivate Confirmation Modal */}
      <Modal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        title="Confirm Room Deletion / Deactivation"
      >
        <div className="confirm-delete-box">
          <AlertTriangle size={36} className="warning-icon" />
          <p>
            Are you sure you want to remove <strong>Room {deletingRoom?.roomNumber}</strong>?
          </p>
          <div className="delete-policy-note">
            <strong>System Rule:</strong>
            <p>
              If this room has never been booked, it will be <em>permanently deleted</em>. If this room is already
              referenced in booking reservation history, its status will automatically be set to <em>Inactive</em> to
              preserve financial and audit records.
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

export default StaffRoomManagement;

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { roomService } from '../../services/roomService';
import { roomTypeService } from '../../services/roomTypeService';
import { useAuth } from '../../context/AuthContext';
import { Search, Filter, BedDouble, Users, Tag, CheckCircle2, XCircle, Eye, Calendar } from 'lucide-react';
import Modal from '../../components/common/Modal';
import Toast from '../../components/common/Toast';

export const RoomsPage = () => {
  const { isAuthenticated, isCustomer } = useAuth();
  const navigate = useNavigate();

  const [rooms, setRooms] = useState([]);
  const [roomTypes, setRoomTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Filters
  const [roomNumber, setRoomNumber] = useState('');
  const [roomTypeId, setRoomTypeId] = useState('');
  const [roomStatus, setRoomStatus] = useState('');
  const [minCapacity, setMinCapacity] = useState('');

  // Modal details
  const [selectedRoom, setSelectedRoom] = useState(null);

  useEffect(() => {
    loadRoomTypes();
    loadRooms();
  }, []);

  const loadRoomTypes = async () => {
    try {
      const data = await roomTypeService.getRoomTypes();
      setRoomTypes(data);
    } catch (err) {
      console.error('Failed to load room types', err);
    }
  };

  const loadRooms = async () => {
    setLoading(true);
    setError('');
    try {
      const params = {};
      if (roomNumber.trim()) params.roomNumber = roomNumber.trim();
      if (roomTypeId) params.roomTypeId = parseInt(roomTypeId, 10);
      if (roomStatus) params.roomStatus = roomStatus;
      if (minCapacity) params.minCapacity = parseInt(minCapacity, 10);

      const data = await roomService.getRooms(params);
      setRooms(data);
    } catch (err) {
      setError('Unable to load rooms. Please check server connection.');
    } finally {
      setLoading(false);
    }
  };

  const handleFilterSubmit = (e) => {
    e.preventDefault();
    loadRooms();
  };

  const handleResetFilters = () => {
    setRoomNumber('');
    setRoomTypeId('');
    setRoomStatus('');
    setMinCapacity('');
    setTimeout(() => {
      roomService.getRooms().then(setRooms).catch(() => {});
    }, 0);
  };

  const handleBookRoom = (room) => {
    if (!isAuthenticated) {
      navigate('/login');
    } else if (isCustomer) {
      navigate(`/customer/booking?roomId=${room.roomID}`);
    } else {
      navigate('/staff/bookings');
    }
  };

  return (
    <div className="page-container">
      <div className="page-hero">
        <div className="hero-content">
          <h1>Find Your Ideal Hotel Room</h1>
          <p>Explore comfortable, modern rooms and suites with world-class amenities</p>
        </div>
      </div>

      <div className="page-content">
        {error && <Toast message={error} type="error" onClose={() => setError('')} />}

        {/* Filter Card */}
        <div className="filter-card">
          <form onSubmit={handleFilterSubmit} className="filter-form">
            <div className="filter-group">
              <label htmlFor="filterRoomNumber">Room Number</label>
              <div className="input-with-icon">
                <Search size={16} className="input-icon" />
                <input
                  id="filterRoomNumber"
                  type="text"
                  placeholder="e.g. 101"
                  value={roomNumber}
                  onChange={(e) => setRoomNumber(e.target.value)}
                />
              </div>
            </div>

            <div className="filter-group">
              <label htmlFor="filterRoomType">Room Type</label>
              <select
                id="filterRoomType"
                value={roomTypeId}
                onChange={(e) => setRoomTypeId(e.target.value)}
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
              <label htmlFor="filterMinCapacity">Min Capacity</label>
              <input
                id="filterMinCapacity"
                type="number"
                min="1"
                placeholder="Guests"
                value={minCapacity}
                onChange={(e) => setMinCapacity(e.target.value)}
              />
            </div>

            <div className="filter-group">
              <label htmlFor="filterStatus">Status</label>
              <select
                id="filterStatus"
                value={roomStatus}
                onChange={(e) => setRoomStatus(e.target.value)}
              >
                <option value="">All Statuses</option>
                <option value="1">Active</option>
                <option value="0">Inactive</option>
              </select>
            </div>

            <div className="filter-actions">
              <button type="submit" className="btn btn-primary">
                <Filter size={16} />
                <span>Search</span>
              </button>
              <button type="button" onClick={handleResetFilters} className="btn btn-secondary">
                Reset
              </button>
            </div>
          </form>
        </div>

        {/* Room Grid */}
        {loading ? (
          <div className="loading-state">Loading hotel rooms...</div>
        ) : rooms.length === 0 ? (
          <div className="empty-state">
            <BedDouble size={48} className="empty-icon" />
            <h3>No rooms matched your criteria</h3>
            <p>Try modifying your search filter parameters or reset to view all available rooms.</p>
          </div>
        ) : (
          <div className="room-grid">
            {rooms.map((room) => {
              const isActive = room.roomStatus === '1' || room.roomStatus === 'Active';
              return (
                <div key={room.roomID} className="room-card">
                  <div className="room-card-header">
                    <div className="room-badge-type">{room.roomType?.roomTypeName || 'Standard'}</div>
                    <span className={`status-pill ${isActive ? 'status-active' : 'status-inactive'}`}>
                      {isActive ? 'Available' : 'Unavailable'}
                    </span>
                  </div>

                  <div className="room-card-body">
                    <h3 className="room-number">Room {room.roomNumber}</h3>
                    <p className="room-desc">{room.roomDetailDescription || 'Cozy and spacious hotel room.'}</p>

                    <div className="room-specs">
                      <div className="spec-item">
                        <Users size={16} />
                        <span>Up to {room.roomMaxCapacity} guests</span>
                      </div>
                      <div className="spec-item">
                        <Tag size={16} />
                        <span className="price-tag">${parseFloat(room.roomPricePerDay).toFixed(2)}</span>
                        <span className="price-unit">/ night</span>
                      </div>
                    </div>
                  </div>

                  <div className="room-card-footer">
                    <button
                      type="button"
                      className="btn btn-outline btn-sm"
                      onClick={() => setSelectedRoom(room)}
                    >
                      <Eye size={16} />
                      <span>Details</span>
                    </button>
                    <button
                      type="button"
                      className="btn btn-primary btn-sm"
                      disabled={!isActive}
                      onClick={() => handleBookRoom(room)}
                    >
                      <Calendar size={16} />
                      <span>Book Now</span>
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* Room Detail Modal */}
        {selectedRoom && (
          <Modal
            isOpen={!!selectedRoom}
            onClose={() => setSelectedRoom(null)}
            title={`Room ${selectedRoom.roomNumber} Details`}
          >
            <div className="room-modal-details">
              <div className="modal-detail-row">
                <strong>Room Type:</strong>
                <span>{selectedRoom.roomType?.roomTypeName}</span>
              </div>
              <div className="modal-detail-row">
                <strong>Type Description:</strong>
                <span>{selectedRoom.roomType?.typeDescription || 'None'}</span>
              </div>
              <div className="modal-detail-row">
                <strong>Type Note:</strong>
                <span>{selectedRoom.roomType?.typeNote || 'None'}</span>
              </div>
              <div className="modal-detail-row">
                <strong>Max Capacity:</strong>
                <span>{selectedRoom.roomMaxCapacity} persons</span>
              </div>
              <div className="modal-detail-row">
                <strong>Nightly Rate:</strong>
                <span className="highlight-price">
                  ${parseFloat(selectedRoom.roomPricePerDay).toFixed(2)} per day
                </span>
              </div>
              <div className="modal-detail-row">
                <strong>Status:</strong>
                <span className={`status-pill ${selectedRoom.roomStatus === '1' ? 'status-active' : 'status-inactive'}`}>
                  {selectedRoom.roomStatus === '1' ? 'Active / Available' : 'Inactive'}
                </span>
              </div>
              <div className="modal-detail-desc">
                <strong>Description:</strong>
                <p>{selectedRoom.roomDetailDescription}</p>
              </div>

              <div className="modal-actions" style={{ marginTop: '20px' }}>
                <button
                  type="button"
                  className="btn btn-primary"
                  disabled={selectedRoom.roomStatus !== '1'}
                  onClick={() => {
                    const r = selectedRoom;
                    setSelectedRoom(null);
                    handleBookRoom(r);
                  }}
                >
                  Book This Room
                </button>
                <button type="button" className="btn btn-secondary" onClick={() => setSelectedRoom(null)}>
                  Close
                </button>
              </div>
            </div>
          </Modal>
        )}
      </div>
    </div>
  );
};

export default RoomsPage;

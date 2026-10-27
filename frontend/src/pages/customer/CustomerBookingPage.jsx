import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { bookingService } from '../../services/bookingService';
import { roomService } from '../../services/roomService';
import { Calendar, BedDouble, Users, CheckSquare, Square, DollarSign, ArrowRight } from 'lucide-react';
import Toast from '../../components/common/Toast';

export const CustomerBookingPage = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const [rooms, setRooms] = useState([]);
  const [loadingRooms, setLoadingRooms] = useState(true);
  const [selectedRoomIds, setSelectedRoomIds] = useState([]);

  // Dates
  const todayStr = new Date().toISOString().split('T')[0];
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const tomorrowStr = tomorrow.toISOString().split('T')[0];

  const [startDate, setStartDate] = useState(todayStr);
  const [endDate, setEndDate] = useState(tomorrowStr);

  const [errors, setErrors] = useState({});
  const [toast, setToast] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadActiveRooms();
  }, []);

  const loadActiveRooms = async () => {
    setLoadingRooms(true);
    try {
      const data = await roomService.getRooms({ roomStatus: '1' });
      setRooms(data);

      // Check query param roomId
      const searchParams = new URLSearchParams(location.search);
      const preSelectedId = searchParams.get('roomId');
      if (preSelectedId) {
        setSelectedRoomIds([parseInt(preSelectedId, 10)]);
      }
    } catch (err) {
      setToast({ type: 'error', message: 'Failed to load available rooms.' });
    } finally {
      setLoadingRooms(false);
    }
  };

  const toggleRoomSelection = (roomId) => {
    setSelectedRoomIds((prev) =>
      prev.includes(roomId) ? prev.filter((id) => id !== roomId) : [...prev, roomId]
    );
  };

  const getNumberOfDays = () => {
    if (!startDate || !endDate) return 0;
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diff = Math.round((end - start) / (1000 * 60 * 60 * 24));
    return diff > 0 ? diff : 0;
  };

  const calculateTotalPrice = () => {
    const days = getNumberOfDays();
    if (days <= 0) return 0;
    return selectedRoomIds.reduce((sum, id) => {
      const room = rooms.find((r) => r.roomID === id);
      return sum + (room ? room.roomPricePerDay * days : 0);
    }, 0);
  };

  const validate = () => {
    const errs = {};
    if (!startDate) {
      errs.startDate = 'Check-in date is required';
    } else if (startDate < todayStr) {
      errs.startDate = 'Check-in date cannot be in the past';
    }

    if (!endDate) {
      errs.endDate = 'Check-out date is required';
    } else if (startDate && endDate <= startDate) {
      errs.endDate = 'Check-out date must be strictly after check-in date';
    }

    if (selectedRoomIds.length === 0) {
      errs.rooms = 'Please select at least one room for your booking';
    }

    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setSubmitting(true);
    try {
      const payload = {
        details: selectedRoomIds.map((id) => ({
          roomID: id,
          startDate,
          endDate,
        })),
      };

      await bookingService.createOnlineBooking(payload);
      setToast({ type: 'success', message: 'Online booking confirmed successfully! Redirecting...' });
      setTimeout(() => {
        navigate('/customer/history');
      }, 1500);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to complete online booking.';
      setToast({ type: 'error', message: msg });
    } finally {
      setSubmitting(false);
    }
  };

  const days = getNumberOfDays();
  const totalPrice = calculateTotalPrice();

  return (
    <div className="page-container">
      <div className="page-hero">
        <div className="hero-content">
          <h1>Online Room Booking</h1>
          <p>Select your stay dates, pick one or multiple rooms, and reserve instantly</p>
        </div>
      </div>

      <div className="page-content">
        {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}

        <form onSubmit={handleSubmit} className="booking-workflow">
          {/* Step 1: Stay Dates */}
          <div className="booking-card">
            <div className="booking-card-header">
              <Calendar className="card-icon" />
              <h3>1. Select Dates of Stay</h3>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="customerCheckIn">Check-In Date *</label>
                <input
                  id="customerCheckIn"
                  type="date"
                  min={todayStr}
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className={errors.startDate ? 'input-error' : ''}
                />
                {errors.startDate && <span className="field-error">{errors.startDate}</span>}
              </div>

              <div className="form-group">
                <label htmlFor="customerCheckOut">Check-Out Date *</label>
                <input
                  id="customerCheckOut"
                  type="date"
                  min={startDate || todayStr}
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className={errors.endDate ? 'input-error' : ''}
                />
                {errors.endDate && <span className="field-error">{errors.endDate}</span>}
              </div>
            </div>

            {days > 0 && (
              <div className="days-info-pill">
                Stay Duration: <strong>{days} night{days > 1 ? 's' : ''}</strong>
              </div>
            )}
          </div>

          {/* Step 2: Choose Room(s) */}
          <div className="booking-card">
            <div className="booking-card-header">
              <BedDouble className="card-icon" />
              <h3>2. Select Rooms ({selectedRoomIds.length} selected)</h3>
            </div>

            {errors.rooms && <p className="field-error" style={{ marginBottom: '12px' }}>{errors.rooms}</p>}

            {loadingRooms ? (
              <div className="loading-state">Loading active rooms...</div>
            ) : rooms.length === 0 ? (
              <p>No active rooms currently available for online booking.</p>
            ) : (
              <div className="room-selection-grid">
                {rooms.map((room) => {
                  const isSelected = selectedRoomIds.includes(room.roomID);
                  const roomActualPrice = days > 0 ? room.roomPricePerDay * days : room.roomPricePerDay;

                  return (
                    <div
                      key={room.roomID}
                      className={`room-select-card ${isSelected ? 'selected' : ''}`}
                      onClick={() => toggleRoomSelection(room.roomID)}
                    >
                      <div className="select-checkbox">
                        {isSelected ? <CheckSquare size={20} className="text-primary" /> : <Square size={20} />}
                      </div>

                      <div className="room-select-info">
                        <div className="room-select-title">
                          <strong>Room {room.roomNumber}</strong>
                          <span className="room-type-tag">{room.roomType?.roomTypeName}</span>
                        </div>
                        <p className="room-select-desc">{room.roomDetailDescription || 'Comfortable room'}</p>

                        <div className="room-select-meta">
                          <span className="meta-item">
                            <Users size={14} /> {room.roomMaxCapacity} Guests
                          </span>
                          <span className="meta-price">
                            ${parseFloat(room.roomPricePerDay).toFixed(2)} / night
                          </span>
                        </div>

                        {days > 0 && isSelected && (
                          <div className="room-calculated-subtotal">
                            Subtotal ({days} nights): <strong>${roomActualPrice.toFixed(2)}</strong>
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Step 3: Order Summary & Checkout */}
          <div className="booking-summary-card">
            <h3>Reservation Summary</h3>
            <div className="summary-details">
              <div className="summary-row">
                <span>Check-In:</span>
                <strong>{startDate || 'Not selected'}</strong>
              </div>
              <div className="summary-row">
                <span>Check-Out:</span>
                <strong>{endDate || 'Not selected'}</strong>
              </div>
              <div className="summary-row">
                <span>Duration:</span>
                <strong>{days} night{days > 1 ? 's' : ''}</strong>
              </div>
              <div className="summary-row">
                <span>Rooms Selected:</span>
                <strong>{selectedRoomIds.length} room(s)</strong>
              </div>

              <div className="summary-divider" />

              <div className="summary-row total-row">
                <span>Total Amount:</span>
                <span className="total-price-value">${totalPrice.toFixed(2)}</span>
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-block btn-lg"
              disabled={submitting || selectedRoomIds.length === 0 || days <= 0}
            >
              {submitting ? 'Confirming Reservation...' : 'Confirm Online Booking'}
              <ArrowRight size={18} />
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CustomerBookingPage;

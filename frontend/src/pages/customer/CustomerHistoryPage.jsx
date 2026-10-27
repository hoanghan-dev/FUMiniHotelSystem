import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { bookingService } from '../../services/bookingService';
import { Calendar, BedDouble, ChevronDown, ChevronUp, Clock, AlertCircle } from 'lucide-react';
import Toast from '../../components/common/Toast';

export const CustomerHistoryPage = () => {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);
  const [expandedId, setExpandedId] = useState(null);

  useEffect(() => {
    loadHistory();
  }, []);

  const loadHistory = async () => {
    setLoading(true);
    try {
      const data = await bookingService.getMyBookingHistory();
      setHistory(data);
    } catch (err) {
      setToast({ type: 'error', message: 'Failed to load booking history.' });
    } finally {
      setLoading(false);
    }
  };

  const toggleExpand = (id) => {
    setExpandedId((prev) => (prev === id ? null : id));
  };

  return (
    <div className="page-container">
      <div className="page-hero">
        <div className="hero-content">
          <h1>My Booking History</h1>
          <p>Review all your past and upcoming reservation details and receipts</p>
        </div>
      </div>

      <div className="page-content">
        {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}

        {loading ? (
          <div className="loading-state">Loading your reservations...</div>
        ) : history.length === 0 ? (
          <div className="empty-state">
            <Calendar size={48} className="empty-icon" />
            <h3>No booking reservations found</h3>
            <p>You haven't made any hotel reservations yet.</p>
            <Link to="/customer/booking" className="btn btn-primary" style={{ marginTop: '16px' }}>
              Book a Room Now
            </Link>
          </div>
        ) : (
          <div className="history-list">
            {history.map((reservation) => {
              const isConfirmed = reservation.bookingStatus === '1' || reservation.bookingStatus === 'Confirmed';
              const isExpanded = expandedId === reservation.bookingReservationID;

              return (
                <div key={reservation.bookingReservationID} className="history-card">
                  <div className="history-card-header" onClick={() => toggleExpand(reservation.bookingReservationID)}>
                    <div className="header-main-info">
                      <div className="reservation-id-badge">
                        Reservation #{reservation.bookingReservationID}
                      </div>
                      <div className="header-meta">
                        <span className="meta-item">
                          <Clock size={16} /> Booked on {reservation.bookingDate}
                        </span>
                        <span className="meta-item">
                          <BedDouble size={16} /> {reservation.bookingDetails?.length || 0} room(s)
                        </span>
                      </div>
                    </div>

                    <div className="header-price-status">
                      <div className="history-price">
                        ${parseFloat(reservation.totalPrice).toFixed(2)}
                      </div>
                      <span className={`status-pill ${isConfirmed ? 'status-active' : 'status-inactive'}`}>
                        {isConfirmed ? 'Confirmed' : 'Cancelled'}
                      </span>
                      <button type="button" className="expand-chevron" aria-label="Toggle details">
                        {isExpanded ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
                      </button>
                    </div>
                  </div>

                  {isExpanded && (
                    <div className="history-card-body">
                      <h4>Room Reservation Details</h4>
                      <div className="history-room-table-container">
                        <table className="data-table">
                          <thead>
                            <tr>
                              <th>Room</th>
                              <th>Type</th>
                              <th>Check-In</th>
                              <th>Check-Out</th>
                              <th>Duration</th>
                              <th>Price / Night</th>
                              <th>Actual Total</th>
                            </tr>
                          </thead>
                          <tbody>
                            {reservation.bookingDetails?.map((detail, idx) => (
                              <tr key={idx}>
                                <td>
                                  <strong>Room {detail.roomNumber}</strong>
                                </td>
                                <td>{detail.roomTypeName || 'Standard'}</td>
                                <td>{detail.startDate}</td>
                                <td>{detail.endDate}</td>
                                <td>{detail.numberOfDays} nights</td>
                                <td>${parseFloat(detail.roomPricePerDay).toFixed(2)}</td>
                                <td>
                                  <strong className="text-success">${parseFloat(detail.actualPrice).toFixed(2)}</strong>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default CustomerHistoryPage;

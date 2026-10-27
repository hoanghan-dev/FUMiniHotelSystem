import React from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Hotel, User, LogOut, LogIn, Calendar, BedDouble, Users, ShieldCheck } from 'lucide-react';

export const Navbar = () => {
  const { user, isAuthenticated, isStaff, isCustomer, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-brand">
          <Hotel className="brand-icon" />
          <span className="brand-name">FU Mini Hotel</span>
        </Link>

        <nav className="nav-links">
          {/* Public Link */}
          <NavLink to="/rooms" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
            <BedDouble size={18} />
            <span>Rooms</span>
          </NavLink>

          {/* Customer Links */}
          {isAuthenticated && isCustomer && (
            <>
              <NavLink to="/customer/booking" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <Calendar size={18} />
                <span>Book a Room</span>
              </NavLink>
              <NavLink to="/customer/history" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <span>My Bookings</span>
              </NavLink>
              <NavLink to="/customer/profile" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <User size={18} />
                <span>My Profile</span>
              </NavLink>
            </>
          )}

          {/* Staff Links */}
          {isAuthenticated && isStaff && (
            <>
              <NavLink to="/staff/customers" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <Users size={18} />
                <span>Customers</span>
              </NavLink>
              <NavLink to="/staff/rooms" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <BedDouble size={18} />
                <span>Room Management</span>
              </NavLink>
              <NavLink to="/staff/bookings" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <Calendar size={18} />
                <span>Booking Reservations</span>
              </NavLink>
            </>
          )}
        </nav>

        <div className="navbar-auth">
          {isAuthenticated ? (
            <div className="user-profile-badge">
              <span className={`role-pill ${isStaff ? 'role-staff' : 'role-customer'}`}>
                {isStaff ? 'STAFF' : 'CUSTOMER'}
              </span>
              <span className="user-name">{user?.fullName || user?.email}</span>
              <button onClick={handleLogout} className="btn-logout" title="Sign out">
                <LogOut size={16} />
                <span>Logout</span>
              </button>
            </div>
          ) : (
            <div className="auth-actions">
              <Link to="/login" className="btn btn-outline">
                <LogIn size={16} />
                <span>Login</span>
              </Link>
              <Link to="/register" className="btn btn-primary">
                <span>Register</span>
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Navbar;

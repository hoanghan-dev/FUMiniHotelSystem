import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/common/Navbar';
import ProtectedRoute from './components/common/ProtectedRoute';
import UnauthorizedPage from './components/common/UnauthorizedPage';

// Public Pages
import RoomsPage from './pages/public/RoomsPage';
import LoginPage from './pages/public/LoginPage';
import RegisterPage from './pages/public/RegisterPage';

// Staff Pages
import StaffRoomManagement from './pages/staff/StaffRoomManagement';
import StaffCustomerManagement from './pages/staff/StaffCustomerManagement';
import StaffBookingManagement from './pages/staff/StaffBookingManagement';

// Customer Pages
import CustomerBookingPage from './pages/customer/CustomerBookingPage';
import CustomerProfilePage from './pages/customer/CustomerProfilePage';
import CustomerHistoryPage from './pages/customer/CustomerHistoryPage';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="app-container">
          <Navbar />
          <main className="main-content">
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<Navigate to="/rooms" replace />} />
              <Route path="/rooms" element={<RoomsPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/unauthorized" element={<UnauthorizedPage />} />

              {/* Staff Only Routes */}
              <Route
                path="/staff/rooms"
                element={
                  <ProtectedRoute allowedRoles={['STAFF']}>
                    <StaffRoomManagement />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/staff/customers"
                element={
                  <ProtectedRoute allowedRoles={['STAFF']}>
                    <StaffCustomerManagement />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/staff/bookings"
                element={
                  <ProtectedRoute allowedRoles={['STAFF']}>
                    <StaffBookingManagement />
                  </ProtectedRoute>
                }
              />

              {/* Customer Only Routes */}
              <Route
                path="/customer/booking"
                element={
                  <ProtectedRoute allowedRoles={['CUSTOMER']}>
                    <CustomerBookingPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/customer/profile"
                element={
                  <ProtectedRoute allowedRoles={['CUSTOMER']}>
                    <CustomerProfilePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/customer/history"
                element={
                  <ProtectedRoute allowedRoles={['CUSTOMER']}>
                    <CustomerHistoryPage />
                  </ProtectedRoute>
                }
              />

              {/* Catch-all Fallback */}
              <Route path="*" element={<Navigate to="/rooms" replace />} />
            </Routes>
          </main>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;

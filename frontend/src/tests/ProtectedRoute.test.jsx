import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import React from 'react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import ProtectedRoute from '../components/common/ProtectedRoute';
import * as AuthContextModule from '../context/AuthContext';

describe('ProtectedRoute Component', () => {
  it('redirects unauthenticated user to login', () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: null,
      isAuthenticated: false,
      loading: false,
    });

    render(
      <MemoryRouter initialEntries={['/protected']}>
        <Routes>
          <Route path="/login" element={<div>Login Page Mock</div>} />
          <Route
            path="/protected"
            element={
              <ProtectedRoute allowedRoles={['STAFF']}>
                <div>Secret Staff Page</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Login Page Mock')).toBeInTheDocument();
    expect(screen.queryByText('Secret Staff Page')).not.toBeInTheDocument();
  });

  it('redirects unauthorized role to unauthorized page', () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: { role: 'CUSTOMER', email: 'cust@hotel.com' },
      isAuthenticated: true,
      loading: false,
    });

    render(
      <MemoryRouter initialEntries={['/staff-only']}>
        <Routes>
          <Route path="/unauthorized" element={<div>Unauthorized Page Mock</div>} />
          <Route
            path="/staff-only"
            element={
              <ProtectedRoute allowedRoles={['STAFF']}>
                <div>Secret Staff Area</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Unauthorized Page Mock')).toBeInTheDocument();
    expect(screen.queryByText('Secret Staff Area')).not.toBeInTheDocument();
  });

  it('renders children when user is authenticated with correct role', () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: { role: 'STAFF', email: 'staff@hotel.com' },
      isAuthenticated: true,
      loading: false,
    });

    render(
      <MemoryRouter initialEntries={['/staff-only']}>
        <Routes>
          <Route
            path="/staff-only"
            element={
              <ProtectedRoute allowedRoles={['STAFF']}>
                <div>Secret Staff Area</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Secret Staff Area')).toBeInTheDocument();
  });
});

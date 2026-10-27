import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldAlert, ArrowLeft } from 'lucide-react';

export const UnauthorizedPage = () => {
  return (
    <div className="error-page-container">
      <div className="error-card">
        <ShieldAlert size={64} className="error-icon" />
        <h2>403 - Access Denied</h2>
        <p>You do not have the required permissions or role to view this page.</p>
        <Link to="/" className="btn btn-primary">
          <ArrowLeft size={16} />
          <span>Back to Home</span>
        </Link>
      </div>
    </div>
  );
};

export default UnauthorizedPage;

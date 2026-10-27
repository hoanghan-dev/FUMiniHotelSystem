import React from 'react';
import { CheckCircle, AlertCircle, Info, X } from 'lucide-react';

export const Toast = ({ message, type = 'info', onClose }) => {
  if (!message) return null;

  const icons = {
    success: <CheckCircle size={20} className="toast-icon success" />,
    error: <AlertCircle size={20} className="toast-icon error" />,
    warning: <AlertCircle size={20} className="toast-icon warning" />,
    info: <Info size={20} className="toast-icon info" />,
  };

  return (
    <div className={`toast-notification toast-${type}`}>
      {icons[type] || icons.info}
      <span className="toast-message">{message}</span>
      {onClose && (
        <button onClick={onClose} className="toast-close" aria-label="Close">
          <X size={16} />
        </button>
      )}
    </div>
  );
};

export default Toast;

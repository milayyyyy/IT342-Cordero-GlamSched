import React, { useState, useEffect } from 'react';
import { apiGet } from '../utils/api';

function MyAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const clientId = localStorage.getItem('userId');

  useEffect(() => {
    const fetchAppointments = async () => {
      try {
        setLoading(true);
        const data = await apiGet(`/api/appointments?clientId=${clientId}`);
        const list = Array.isArray(data) ? data : (data.data || []);
        if (Array.isArray(list)) {
          setAppointments(list);
          setError('');
        } else {
          setAppointments([]);
          setError('Failed to load appointments');
        }
      } catch (err) {
        setAppointments([]);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    if (clientId) {
      fetchAppointments();
    }
  }, [clientId]);

  const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case 'CONFIRMED':
        return '#4caf50';
      case 'PENDING':
        return '#ff9800';
      case 'CANCELLED':
        return '#dc3545';
      default:
        return '#999';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return dateString;
    }
  };

  if (loading) {
    return (
      <div className="page-section">
        <h2 className="section-title">My Appointments</h2>
        <p className="section-sub">View your scheduled appointments</p>
        <div className="empty-state">
          <p>Loading appointments...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-section">
      <h2 className="section-title">My Appointments</h2>
      <p className="section-sub">View and manage your scheduled appointments</p>

      {error && (
        <div className="error-state">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
          </svg>
          {error}
        </div>
      )}

      {appointments.length === 0 ? (
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
          <p>No appointments yet</p>
          <p className="empty-sub">Book your first appointment to get started!</p>
        </div>
      ) : (
        <div className="appointments-list">
          {appointments.map(appointment => (
            <div key={appointment.id} className="appointment-card">
              <div className="appointment-header">
                <h3 className="appointment-service">{appointment.service?.name || 'Service'}</h3>
                <span 
                  className="status-badge"
                  style={{ 
                    backgroundColor: getStatusColor(appointment.status),
                    color: 'white',
                    padding: '4px 12px',
                    borderRadius: '20px',
                    fontSize: '12px',
                    fontWeight: '600',
                    textTransform: 'capitalize'
                  }}
                >
                  {appointment.status?.toLowerCase() || 'pending'}
                </span>
              </div>

              <div className="appointment-details-row">
                <div className="detail-box">
                  <div className="detail-icon">📅</div>
                  <div className="detail-content">
                    <div className="detail-label">Date & Time</div>
                    <div className="detail-value">{formatDate(appointment.appointmentDate)}</div>
                  </div>
                </div>

                <div className="detail-box">
                  <div className="detail-icon">💅</div>
                  <div className="detail-content">
                    <div className="detail-label">Artist</div>
                    <div className="detail-value">{appointment.artist?.name || 'N/A'}</div>
                  </div>
                </div>
              </div>

              {appointment.notes && (
                <div className="appointment-notes">
                  <strong>Notes:</strong> {appointment.notes}
                </div>
              )}

              <div className="appointment-actions">
                <button className="btn-secondary" title="View details" onClick={() => setSelectedAppointment(appointment)}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                  </svg>
                  View
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
      
      {selectedAppointment && (
        <div className="modal-overlay" onClick={() => setSelectedAppointment(null)} style={{position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000}}>
          <div className="modal-content" onClick={e => e.stopPropagation()} style={{backgroundColor: 'white', padding: '32px', borderRadius: '12px', maxWidth: '500px', boxShadow: '0 10px 40px rgba(0,0,0,0.2)'}}>
            <button className="modal-close" onClick={() => setSelectedAppointment(null)} style={{position: 'absolute', top: '16px', right: '16px', background: 'none', border: 'none', fontSize: '24px', cursor: 'pointer'}}>✕</button>
            <h3 style={{marginTop: 0}}>{selectedAppointment.service?.name || 'Appointment Details'}</h3>
            <div className="appointment-detail" style={{marginBottom: '20px'}}>
              <p><strong>Date & Time:</strong> {formatDate(selectedAppointment.appointmentDate)}</p>
              <p><strong>Artist:</strong> {selectedAppointment.artist?.name || 'N/A'}</p>
              <p><strong>Status:</strong> <span style={{color: getStatusColor(selectedAppointment.status), fontWeight: '600'}}>{selectedAppointment.status}</span></p>
              {selectedAppointment.notes && <p><strong>Notes:</strong> {selectedAppointment.notes}</p>}
            </div>
            <button className="btn-primary" onClick={() => setSelectedAppointment(null)} style={{width: '100%', padding: '12px', background: '#d4af37', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: '600'}}>Close</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default MyAppointments;

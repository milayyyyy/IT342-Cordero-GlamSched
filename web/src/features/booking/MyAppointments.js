import React, { useState, useEffect } from 'react';
import { apiGet } from '../../shared/api';

function MyAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [activeFilter, setActiveFilter] = useState('all');
  const clientId = localStorage.getItem('userId');

  useEffect(() => {
    const fetchAppointments = async () => {
      try {
        setLoading(true);
        const data = await apiGet(`/api/appointments?clientId=${clientId}`);
        const list = Array.isArray(data) ? data : (data.data || []);

        // Build price map from all services
        const priceMap = {};
        try {
          const svcRes = await apiGet('/api/services');
          const allServices = Array.isArray(svcRes) ? svcRes : (svcRes?.data || []);
          allServices.forEach(s => { if (s.id != null && s.price != null) priceMap[s.id] = Number.parseFloat(s.price) || 0; });
        } catch { /* ignore */ }

        const enriched = list.map(a => ({ ...a, price: priceMap[a.serviceId] ?? null }));
        setAppointments(enriched);
        setError('');
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
      case 'CONFIRMED':  return '#4caf50';
      case 'PENDING':    return '#ff9800';
      case 'CANCELLED':  return '#dc3545';
      case 'COMPLETED':  return '#d4af37';
      default:           return '#999';
    }
  };

  const formatDateTime = (dateStr, timeStr) => {
    if (!dateStr) return 'Not set';
    try {
      const d = new Date(dateStr);
      const datePart = isNaN(d.getTime())
        ? dateStr
        : d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' });

      if (!timeStr) return datePart;

      // Convert 24hr time string (HH:MM or HH:MM:SS) to 12hr format
      const [hourStr, minuteStr] = timeStr.split(':');
      const hour = parseInt(hourStr, 10);
      const minute = minuteStr || '00';
      const period = hour >= 12 ? 'PM' : 'AM';
      const hour12 = hour % 12 || 12;
      return `${datePart} at ${hour12}:${minute} ${period}`;
    } catch {
      return dateStr + (timeStr ? ` at ${timeStr}` : '');
    }
  };

  if (loading) {
    return (
      <div className="page-section">
        <h2 className="section-title">My Appointments</h2>
        <p className="section-sub">View your scheduled appointments</p>
        <div className="empty-state"><p>Loading appointments...</p></div>
      </div>
    );
  }

  const filters = ['all', 'confirmed', 'pending', 'cancelled', 'completed'];
  const displayed = activeFilter === 'all'
    ? appointments
    : appointments.filter(a => a.status?.toUpperCase() === activeFilter.toUpperCase());

  const statusBorderClass = (status) => {
    switch (status?.toUpperCase()) {
      case 'CONFIRMED':  return 'appt-status-confirmed';
      case 'PENDING':    return 'appt-status-pending';
      case 'CANCELLED':  return 'appt-status-cancelled';
      case 'COMPLETED':  return 'appt-status-completed';
      default: return '';
    }
  };

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

      {/* Filter Tabs */}
      <div className="filter-tabs-row">
        {filters.map(f => (
          <button
            key={f}
            className={`filter-tab-btn${activeFilter === f ? ' active' : ''}`}
            onClick={() => setActiveFilter(f)}
          >
            {f.charAt(0).toUpperCase() + f.slice(1)}
            {f !== 'all' && (
              <span className="filter-tab-count">
                {appointments.filter(a => a.status?.toUpperCase() === f.toUpperCase()).length}
              </span>
            )}
          </button>
        ))}
      </div>

      {appointments.length === 0 && (
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
          <p>No appointments yet</p>
          <p className="empty-sub">Book your first appointment to get started!</p>
        </div>
      )}
      {appointments.length > 0 && displayed.length === 0 && (
        <div className="empty-state">
          <p>No {activeFilter} appointments</p>
        </div>
      )}
      {displayed.length > 0 && (
        <div className="appointments-list">
          {displayed.map(appointment => (
            <div key={appointment.id} className={`appointment-card ${statusBorderClass(appointment.status)}`}>
              <div className="appointment-header">
                <h3 className="appointment-service">{appointment.serviceName || 'Service'}</h3>
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
                  <div className="detail-icon">💰</div>
                  <div className="detail-content">
                    <div className="detail-label">Price</div>
                    <div className="detail-value">
                      {appointment.price != null ? `₱${Number(appointment.price).toLocaleString('en-US', { minimumFractionDigits: 2 })}` : 'N/A'}
                    </div>
                  </div>
                </div>
                <div className="detail-box">
                  <div className="detail-icon">💅</div>
                  <div className="detail-content">
                    <div className="detail-label">Artist</div>
                    <div className="detail-value">{appointment.artistName || 'N/A'}</div>
                  </div>
                </div>
              </div>
              <div className="appointment-details-row">
                <div className="detail-box" style={{gridColumn: '1 / -1'}}>
                  <div className="detail-icon">📅</div>
                  <div className="detail-content">
                    <div className="detail-label">Date & Time</div>
                    <div className="detail-value">{formatDateTime(appointment.date, appointment.time)}</div>
                  </div>
                </div>
              </div>

              {appointment.notes && (
                <div className="appointment-notes">
                  <strong>Notes:</strong> {appointment.notes}
                </div>
              )}

              <div className="appointment-actions">
                <button className="btn-secondary" onClick={() => setSelectedAppointment(appointment)}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                  </svg>
                  View Details
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
      
      {selectedAppointment && (
        <dialog
          open
          className="modal-content"
          style={{position: 'fixed', inset: 0, zIndex: 1000, margin: 'auto', padding: '32px', borderRadius: '12px', maxWidth: '500px', border: 'none', boxShadow: '0 10px 40px rgba(0,0,0,0.2)'}}
        >
          <button className="modal-close" onClick={() => setSelectedAppointment(null)} style={{position: 'absolute', top: '16px', right: '16px', background: 'none', border: 'none', fontSize: '24px', cursor: 'pointer'}}>✕</button>
          <h3 style={{marginTop: 0}}>{selectedAppointment.serviceName || 'Appointment Details'}</h3>
          <div className="appointment-detail" style={{marginBottom: '20px'}}>
            <p><strong>Booking ID:</strong> #{selectedAppointment.id}</p>
            <p><strong>Service:</strong> {selectedAppointment.serviceName || 'N/A'}</p>
            <p><strong>Artist:</strong> {selectedAppointment.artistName || 'N/A'}</p>
            <p><strong>Date & Time:</strong> {formatDateTime(selectedAppointment.date, selectedAppointment.time)}</p>
            <p><strong>Price:</strong> {selectedAppointment.price != null ? `₱${Number(selectedAppointment.price).toLocaleString('en-US', { minimumFractionDigits: 2 })}` : 'N/A'}</p>
            <p><strong>Status:</strong> <span style={{color: getStatusColor(selectedAppointment.status), fontWeight: '600'}}>{selectedAppointment.status}</span></p>
            {selectedAppointment.notes && <p><strong>Notes:</strong> {selectedAppointment.notes}</p>}
          </div>
          <button className="btn-primary" onClick={() => setSelectedAppointment(null)} style={{width: '100%', padding: '12px', background: '#d4af37', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: '600'}}>Close</button>
        </dialog>
      )}
    </div>
  );
}

export default MyAppointments;

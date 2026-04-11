import React, { useState, useEffect, useCallback } from 'react';
import '../styles/ArtistAppointments.css';
import { apiGet, apiPut } from '../utils/api';

function ArtistAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [filteredAppointments, setFilteredAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [updatingId, setUpdatingId] = useState(null);
  const [statusFilter, setStatusFilter] = useState('PENDING'); // PENDING, CONFIRMED, COMPLETED, CANCELLED
  const artistId = localStorage.getItem('userId');

  const fetchAppointments = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await apiGet(`/api/appointments/artist/${artistId}`);
      const list = Array.isArray(data) ? data : (data.data || []);
      setAppointments(list);
    } catch (err) {
      setError(err.message || 'Failed to load appointments');
      setAppointments([]);
    } finally {
      setLoading(false);
    }
  }, [artistId]);

  useEffect(() => {
    if (artistId) {
      fetchAppointments();
    }
  }, [artistId, fetchAppointments]);

  useEffect(() => {
    // Filter appointments based on status
    if (statusFilter === 'ALL') {
      setFilteredAppointments(appointments);
    } else {
      setFilteredAppointments(appointments.filter(apt => apt.status === statusFilter));
    }
  }, [appointments, statusFilter]);

  const handleApproveAppointment = async (appointmentId) => {
    try {
      setUpdatingId(appointmentId);
      await apiPut(`/api/appointments/${appointmentId}/status?status=CONFIRMED`, {});
      // Refresh the list
      fetchAppointments();
    } catch (err) {
      setError(err.message || 'Failed to approve appointment');
    } finally {
      setUpdatingId(null);
    }
  };

  const handleCancelAppointment = async (appointmentId) => {
    try {
      setUpdatingId(appointmentId);
      await apiPut(`/api/appointments/${appointmentId}/status?status=CANCELLED`, {});
      // Refresh the list
      fetchAppointments();
    } catch (err) {
      setError(err.message || 'Failed to cancel appointment');
    } finally {
      setUpdatingId(null);
    }
  };

  const formatDateTime = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'PENDING':
        return 'status-badge status-pending';
      case 'CONFIRMED':
        return 'status-badge status-confirmed';
      case 'COMPLETED':
        return 'status-badge status-completed';
      case 'CANCELLED':
        return 'status-badge status-cancelled';
      default:
        return 'status-badge';
    }
  };

  return (
    <div className="page-section artist-appointments">
      <div className="appointments-header">
        <div>
          <h2 className="section-title">Manage Bookings</h2>
          <p className="section-sub">Review and approve client bookings for your services</p>
        </div>
      </div>

      {/* Status Filter */}
      <div className="status-filter">
        {['PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'ALL'].map(status => (
          <button
            key={status}
            className={`filter-btn ${statusFilter === status ? 'active' : ''}`}
            onClick={() => setStatusFilter(status)}
          >
            {status === 'ALL' ? 'All' : status.charAt(0) + status.slice(1).toLowerCase()}
            {status !== 'ALL' && (
              <span className="filter-count">
                ({appointments.filter(a => a.status === status).length})
              </span>
            )}
          </button>
        ))}
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading && (
        <div className="empty-state">
          <p>Loading appointments...</p>
        </div>
      )}

      {!loading && filteredAppointments.length === 0 && (
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
          <p>{statusFilter === 'PENDING' ? 'No pending bookings' : `No ${statusFilter.toLowerCase()} appointments`}</p>
        </div>
      )}

      {!loading && filteredAppointments.length > 0 && (
        <div className="appointments-grid">
          {filteredAppointments.map(appointment => (
            <div key={appointment.id} className={`appointment-card appointment-${appointment.status.toLowerCase()}`}>
              <div className="appointment-card-header">
                <div>
                  <h3 className="client-name">👤 {appointment.clientName}</h3>
                  <span className={getStatusBadgeClass(appointment.status)}>
                    {appointment.status}
                  </span>
                </div>
                <span className="appointment-id">#{appointment.id}</span>
              </div>

              <div className="appointment-details">
                <div className="detail-row">
                  <span className="detail-label">Service:</span>
                  <span className="detail-value">{appointment.serviceName}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Date & Time:</span>
                  <span className="detail-value">{formatDateTime(appointment.appointmentDate)}</span>
                </div>
                {appointment.notes && (
                  <div className="detail-row">
                    <span className="detail-label">Notes:</span>
                    <span className="detail-value notes">{appointment.notes}</span>
                  </div>
                )}
              </div>

              <div className="appointment-actions">
                {appointment.status === 'PENDING' && (
                  <>
                    <button
                      className="btn-approve"
                      onClick={() => handleApproveAppointment(appointment.id)}
                      disabled={updatingId === appointment.id}
                      title="Approve this booking"
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <polyline points="20 6 9 17 4 12"/>
                      </svg>
                      {updatingId === appointment.id ? 'Approving...' : 'Approve'}
                    </button>
                    <button
                      className="btn-cancel"
                      onClick={() => handleCancelAppointment(appointment.id)}
                      disabled={updatingId === appointment.id}
                      title="Cancel this booking"
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <line x1="18" y1="6" x2="6" y2="18"/>
                        <line x1="6" y1="6" x2="18" y2="18"/>
                      </svg>
                      {updatingId === appointment.id ? 'Cancelling...' : 'Decline'}
                    </button>
                  </>
                )}
                {appointment.status === 'CONFIRMED' && (
                  <button
                    className="btn-cancel-confirmed"
                    onClick={() => handleCancelAppointment(appointment.id)}
                    disabled={updatingId === appointment.id}
                    title="Cancel this appointment"
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <line x1="18" y1="6" x2="6" y2="18"/>
                      <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                    {updatingId === appointment.id ? 'Cancelling...' : 'Cancel'}
                  </button>
                )}
                {(appointment.status === 'COMPLETED' || appointment.status === 'CANCELLED') && (
                  <span className="status-info">
                    {appointment.status === 'COMPLETED' ? '✓ Completed' : '✕ Cancelled'}
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default ArtistAppointments;

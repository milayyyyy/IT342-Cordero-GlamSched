import React, { useState, useEffect, useCallback } from 'react';
import './ArtistAppointments.css';
import { apiGet, apiPut } from '../../shared/api';

function ArtistAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [filteredAppointments, setFilteredAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [updatingId, setUpdatingId] = useState(null);
  const [statusFilter, setStatusFilter] = useState('PENDING');
  const artistId = localStorage.getItem('userId');

  const fetchAppointments = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await apiGet(`/api/appointments/artist/${artistId}`);
      const list = Array.isArray(data) ? data : (data.data || []);

      const needsName = list.filter(a => !a.clientName && a.clientId);
      const uniqueIds = [...new Set(needsName.map(a => a.clientId))];
      const nameMap = {};
      await Promise.all(uniqueIds.map(async (cid) => {
        try {
          const res = await apiGet(`/api/users/${cid}`);
          const u = res?.data || res;
          if (u?.name) nameMap[cid] = u.name;
          else if (u?.fullName) nameMap[cid] = u.fullName;
        } catch { /* ignore */ }
      }));

      const enriched = list.map(a => ({
        ...a,
        clientName: a.clientName || nameMap[a.clientId] || null,
      }));
      setAppointments(enriched);
    } catch (err) {
      setError(err.message || 'Failed to load appointments');
      setAppointments([]);
    } finally {
      setLoading(false);
    }
  }, [artistId]);

  useEffect(() => {
    if (artistId) fetchAppointments();
  }, [artistId, fetchAppointments]);

  useEffect(() => {
    if (statusFilter === 'ALL') {
      setFilteredAppointments(appointments);
    } else {
      setFilteredAppointments(appointments.filter(apt => apt.status === statusFilter));
    }
  }, [appointments, statusFilter]);

  const handleApprove = async (id) => {
    try {
      setUpdatingId(id);
      await apiPut(`/api/appointments/${id}/status?status=CONFIRMED`, {});
      fetchAppointments();
    } catch (err) {
      setError(err.message || 'Failed to approve appointment');
    } finally {
      setUpdatingId(null);
    }
  };

  const handleComplete = async (id) => {
    try {
      setUpdatingId(id);
      await apiPut(`/api/appointments/${id}/status?status=COMPLETED`, {});
      fetchAppointments();
    } catch (err) {
      setError(err.message || 'Failed to mark as completed');
    } finally {
      setUpdatingId(null);
    }
  };

  const handleCancel = async (id) => {
    try {
      setUpdatingId(id);
      await apiPut(`/api/appointments/${id}/status?status=CANCELLED`, {});
      fetchAppointments();
    } catch (err) {
      setError(err.message || 'Failed to cancel appointment');
    } finally {
      setUpdatingId(null);
    }
  };

  const formatDate = (dateStr, timeStr) => {
    if (!dateStr) return 'Not set';
    try {
      const d = new Date(dateStr + (timeStr ? 'T' + timeStr : ''));
      if (isNaN(d.getTime())) return dateStr + (timeStr ? ' at ' + timeStr : '');
      return d.toLocaleDateString('en-US', {
        weekday: 'short', month: 'short', day: 'numeric', year: 'numeric',
      }) + (timeStr ? ' at ' + timeStr : '');
    } catch {
      return dateStr + (timeStr ? ' at ' + timeStr : '');
    }
  };

  const statusCounts = {
    PENDING: appointments.filter(a => a.status === 'PENDING').length,
    CONFIRMED: appointments.filter(a => a.status === 'CONFIRMED').length,
    COMPLETED: appointments.filter(a => a.status === 'COMPLETED').length,
    CANCELLED: appointments.filter(a => a.status === 'CANCELLED').length,
  };

  return (
    <div className="page-section artist-appointments">
      <div className="aa-header">
        <h2 className="section-title">Manage Bookings</h2>
        <p className="section-sub">Review and approve client bookings for your services</p>
      </div>

      <div className="aa-filters">
        {['PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'ALL'].map(status => (
          <button
            key={status}
            className={`aa-filter-btn${statusFilter === status ? ' active' : ''}`}
            onClick={() => setStatusFilter(status)}
          >
            {status === 'ALL' ? 'All' : status.charAt(0) + status.slice(1).toLowerCase()}
            {status !== 'ALL' && <span className="aa-filter-count">{statusCounts[status]}</span>}
          </button>
        ))}
      </div>

      {error && <div className="aa-alert">{error}</div>}

      {loading && (
        <div className="empty-state"><p>Loading appointments...</p></div>
      )}

      {!loading && filteredAppointments.length === 0 && (
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
          <p>{statusFilter === 'ALL' ? 'No appointments yet' : `No ${statusFilter.toLowerCase()} bookings`}</p>
        </div>
      )}

      {!loading && filteredAppointments.length > 0 && (
        <div className="aa-grid">
          {filteredAppointments.map(apt => (
            <div key={apt.id} className={`aa-card aa-status-${apt.status.toLowerCase()}`}>
              {/* Top: Client + Status */}
              <div className="aa-card-top">
                <div className="aa-client-row">
                  <div className="aa-client-avatar">
                    {(apt.clientName || 'C').charAt(0).toUpperCase()}
                  </div>
                  <div className="aa-client-info">
                    <span className="aa-client-name">{apt.clientName || `Client #${apt.clientId}`}</span>
                    <span className="aa-booking-id">Booking #{apt.id}</span>
                  </div>
                </div>
                <span className={`aa-status-badge aa-badge-${apt.status.toLowerCase()}`}>
                  {apt.status}
                </span>
              </div>

              {/* Details */}
              <div className="aa-details">
                <div className="aa-detail-item">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/>
                  </svg>
                  <div>
                    <span className="aa-detail-label">Service</span>
                    <span className="aa-detail-value">{apt.serviceName || 'Not specified'}</span>
                  </div>
                </div>

                <div className="aa-detail-item">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/>
                    <line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
                  </svg>
                  <div>
                    <span className="aa-detail-label">Date & Time</span>
                    <span className="aa-detail-value">{formatDate(apt.date, apt.time)}</span>
                  </div>
                </div>

                {apt.notes && (
                  <div className="aa-detail-item">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                      <polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/>
                      <line x1="16" y1="17" x2="8" y2="17"/>
                    </svg>
                    <div>
                      <span className="aa-detail-label">Notes</span>
                      <span className="aa-detail-value aa-notes">{apt.notes}</span>
                    </div>
                  </div>
                )}
              </div>

              {/* Actions */}
              <div className="aa-actions">
                {apt.status === 'PENDING' && (
                  <>
                    <button className="aa-btn aa-btn-approve" onClick={() => handleApprove(apt.id)} disabled={updatingId === apt.id}>
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                        <polyline points="20 6 9 17 4 12"/>
                      </svg>
                      {updatingId === apt.id ? 'Approving...' : 'Approve'}
                    </button>
                    <button className="aa-btn aa-btn-decline" onClick={() => handleCancel(apt.id)} disabled={updatingId === apt.id}>
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                        <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                      </svg>
                      {updatingId === apt.id ? 'Declining...' : 'Decline'}
                    </button>
                  </>
                )}
                {apt.status === 'CONFIRMED' && (
                  <>
                    <button className="aa-btn aa-btn-complete" onClick={() => handleComplete(apt.id)} disabled={updatingId === apt.id}>
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                        <polyline points="20 6 9 17 4 12"/>
                      </svg>
                      {updatingId === apt.id ? 'Updating...' : 'Mark as Completed'}
                    </button>
                    <button className="aa-btn aa-btn-cancel" onClick={() => handleCancel(apt.id)} disabled={updatingId === apt.id}>
                      {updatingId === apt.id ? 'Cancelling...' : 'Cancel'}
                    </button>
                  </>
                )}
                {(apt.status === 'COMPLETED' || apt.status === 'CANCELLED') && (
                  <div className="aa-status-final">
                    {apt.status === 'COMPLETED' ? '✓ Completed' : '✕ Cancelled'}
                  </div>
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

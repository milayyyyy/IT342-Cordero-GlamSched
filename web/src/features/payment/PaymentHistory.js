import React, { useState, useEffect } from 'react';
import { apiGet } from '../../shared/api';

function PaymentHistory() {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeFilter, setActiveFilter] = useState('all');
  const clientId = localStorage.getItem('userId');

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        setLoading(true);
        const data = await apiGet(`/api/appointments?clientId=${clientId}`);
        const appts = Array.isArray(data) ? data : (data.data || []);

        // Build price map from all services
        const priceMap = {};
        try {
          const svcRes = await apiGet('/api/services');
          const allServices = Array.isArray(svcRes) ? svcRes : (svcRes?.data || []);
          allServices.forEach(s => { if (s.id != null && s.price != null) priceMap[s.id] = Number.parseFloat(s.price) || 0; });
        } catch { /* ignore */ }

        const enriched = appts.map(a => ({
          ...a,
          amount: priceMap[a.serviceId] || 0,
        }));

        setRecords(enriched);
        setError('');
      } catch (err) {
        setRecords([]);
        setError(err.message || 'Failed to load history');
      } finally {
        setLoading(false);
      }
    };

    if (clientId) fetchHistory();
  }, [clientId]);

  const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case 'COMPLETED': return '#4caf50';
      case 'CONFIRMED': return '#d4af37';
      case 'PENDING':   return '#ff9800';
      case 'CANCELLED': return '#dc3545';
      default:          return '#999';
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    try {
      return new Date(dateStr).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
    } catch { return dateStr; }
  };

  const formatPHP = (amount) =>
    `₱${Number(amount || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

  const FILTERS = ['all', 'completed', 'confirmed', 'pending', 'cancelled'];

  const displayed = activeFilter === 'all'
    ? records
    : records.filter(r => r.status?.toUpperCase() === activeFilter.toUpperCase());

  const completedRecords = records.filter(r => r.status?.toUpperCase() === 'COMPLETED');
  const totalSpent = completedRecords.reduce((sum, r) => sum + r.amount, 0);

  if (loading) {
    return (
      <div className="page-section">
        <h2 className="section-title">Payment History</h2>
        <p className="section-sub">View your past payments and bookings</p>
        <div className="empty-state"><p>Loading history...</p></div>
      </div>
    );
  }

  return (
    <div className="page-section">
      <h2 className="section-title">Payment History</h2>
      <p className="section-sub">Track all your bookings and payments</p>

      {error && (
        <div className="error-state">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
          </svg>
          {error}
        </div>
      )}

      {records.length === 0 ? (
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/>
          </svg>
          <p>No booking history yet</p>
          <p className="empty-sub">Your bookings will appear here once you book a service</p>
        </div>
      ) : (
        <>
          {/* Summary cards */}
          <div className="payment-summary">
            <div className="summary-card">
              <span className="summary-label">Total Spent</span>
              <span className="summary-value">{formatPHP(totalSpent)}</span>
            </div>
            <div className="summary-card">
              <span className="summary-label">Total Bookings</span>
              <span className="summary-value">{records.length}</span>
            </div>
            <div className="summary-card">
              <span className="summary-label">Completed</span>
              <span className="summary-value">{completedRecords.length}</span>
            </div>
          </div>

          {/* Filter tabs */}
          <div className="filter-tabs-row">
            {FILTERS.map(f => (
              <button
                key={f}
                className={`filter-tab-btn${activeFilter === f ? ' active' : ''}`}
                onClick={() => setActiveFilter(f)}
              >
                {f.charAt(0).toUpperCase() + f.slice(1)}
                {f !== 'all' && (
                  <span className="filter-tab-count">
                    {records.filter(r => r.status?.toUpperCase() === f.toUpperCase()).length}
                  </span>
                )}
              </button>
            ))}
          </div>

          {displayed.length === 0 ? (
            <div className="empty-state">
              <p>No {activeFilter} bookings</p>
            </div>
          ) : (
            <div className="payments-table-container">
              <table className="payments-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Service</th>
                    <th>Artist</th>
                    <th>Amount</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {displayed.map(r => (
                    <tr key={r.id} className="payment-row">
                      <td className="cell-date">{formatDate(r.date || r.createdAt)}</td>
                      <td className="cell-service">{r.serviceName || 'Service'}</td>
                      <td className="cell-artist">{r.artistName || 'N/A'}</td>
                      <td className="cell-amount">{r.amount > 0 ? formatPHP(r.amount) : '—'}</td>
                      <td className="cell-status">
                        <span
                          className="status-badge-table"
                          style={{
                            background: getStatusColor(r.status),
                            color: 'white',
                            padding: '4px 12px',
                            borderRadius: '20px',
                            fontSize: '12px',
                            fontWeight: '600',
                            textTransform: 'capitalize',
                            display: 'inline-block',
                          }}
                        >
                          {r.status?.toLowerCase() || 'pending'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}

      <div style={{ marginTop: '24px', textAlign: 'center', color: '#aaa', fontSize: '13px' }}>
        <p>For inquiries regarding your payments, please contact our support team.</p>
      </div>
    </div>
  );
}

export default PaymentHistory;

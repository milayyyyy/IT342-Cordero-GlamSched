import React, { useState, useEffect } from 'react';
import { apiGet } from '../utils/api';

function PaymentHistory() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const clientId = localStorage.getItem('userId');

  useEffect(() => {
    const fetchPayments = async () => {
      try {
        setLoading(true);
        const data = await apiGet(`/api/payments?clientId=${clientId}`);
        const list = Array.isArray(data) ? data : (data.data || []);
        if (Array.isArray(list)) {
          setPayments(list);
          setError('');
        } else {
          setPayments([]);
          setError('Failed to load payment history');
        }
      } catch (err) {
        setPayments([]);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    if (clientId) {
      fetchPayments();
    }
  }, [clientId]);

  const getPaymentStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case 'COMPLETED':
        return '#4caf50';
      case 'PENDING':
        return '#ff9800';
      case 'FAILED':
        return '#dc3545';
      case 'REFUNDED':
        return '#2196f3';
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
        day: 'numeric'
      });
    } catch {
      return dateString;
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount || 0);
  };

  if (loading) {
    return (
      <div className="page-section">
        <h2 className="section-title">Payment History</h2>
        <p className="section-sub">View your past payments</p>
        <div className="empty-state">
          <p>Loading payment history...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-section">
      <h2 className="section-title">Payment History</h2>
      <p className="section-sub">Track all your payments and transactions</p>

      {error && (
        <div className="error-state">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
          </svg>
          {error}
        </div>
      )}

      {payments.length === 0 ? (
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/>
          </svg>
          <p>No payment history yet</p>
          <p className="empty-sub">Your payments will appear here once you book services</p>
        </div>
      ) : (
        <>
          <div className="payment-summary">
            <div className="summary-card">
              <span className="summary-label">Total Spent</span>
              <span className="summary-value">
                {formatCurrency(payments.reduce((sum, p) => sum + (parseFloat(p.amount) || 0), 0))}
              </span>
            </div>
            <div className="summary-card">
              <span className="summary-label">Total Payments</span>
              <span className="summary-value">{payments.length}</span>
            </div>
            <div className="summary-card">
              <span className="summary-label">Completed</span>
              <span className="summary-value">
                {payments.filter(p => p.status?.toUpperCase() === 'COMPLETED').length}
              </span>
            </div>
          </div>

          <div className="payments-table-container">
            <table className="payments-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Service</th>
                  <th>Artist</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {payments.map(payment => (
                  <tr key={payment.id} className="payment-row">
                    <td className="cell-date">{formatDate(payment.createdAt || payment.date)}</td>
                    <td className="cell-service">{payment.service?.name || payment.serviceName || 'Service'}</td>
                    <td className="cell-artist">{payment.artist?.name || payment.artistName || 'N/A'}</td>
                    <td className="cell-amount">{formatCurrency(payment.amount)}</td>
                    <td className="cell-status">
                      <span 
                        className="status-badge-table"
                        style={{ 
                          backgroundColor: getPaymentStatusColor(payment.status),
                          color: 'white',
                          padding: '6px 12px',
                          borderRadius: '20px',
                          fontSize: '12px',
                          fontWeight: '600',
                          textTransform: 'capitalize',
                          display: 'inline-block'
                        }}
                      >
                        {payment.status?.toLowerCase() || 'pending'}
                      </span>
                    </td>
                    <td className="cell-action">
                      <button className="btn-icon" title="Download receipt" onClick={() => alert('Receipt generated for payment ID: ' + payment.id)} style={{cursor: 'pointer'}}>
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
                        </svg>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}

export default PaymentHistory;

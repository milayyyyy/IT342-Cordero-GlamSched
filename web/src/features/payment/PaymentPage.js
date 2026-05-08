import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './PaymentPage.css';
import { apiPost } from '../../shared/api';

const PAYMENT_METHODS = [
  {
    key: 'card',
    label: 'Credit / Debit Card',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/>
      </svg>
    ),
  },
  {
    key: 'ewallet',
    label: 'E-Wallet',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M2 7a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V7z"/>
        <circle cx="17" cy="12" r="1.5"/>
      </svg>
    ),
  },
  {
    key: 'walkin',
    label: 'Walk-in / Pay Later',
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
      </svg>
    ),
  },
];

const EWALLETS = ['GCash', 'Maya', 'ShopeePay', 'GrabPay'];

const PaymentPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const bookingData = location.state || {};

  const [paymentStep, setPaymentStep] = useState('form');
  const [paymentMethod, setPaymentMethod] = useState('card');
  const [formData, setFormData] = useState({ cardholderName: '', cardNumber: '', expiryDate: '', cvv: '' });
  const [ewalletType, setEwalletType] = useState('GCash');
  const [ewalletNumber, setEwalletNumber] = useState('');
  const [loading, setLoading] = useState(false);
  const [bookingRef, setBookingRef] = useState('');

  const totalAmount = bookingData.service ? Number.parseFloat(bookingData.service.price).toFixed(2) : '0.00';

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    if (name === 'cardNumber') {
      const cleaned = value.replaceAll(/\D/g, '');
      const formatted = cleaned.slice(0, 16).replace(/(\d{4})/g, '$1 ').trim();
      setFormData({ ...formData, [name]: formatted });
    } else if (name === 'expiryDate') {
      let cleaned = value.replaceAll(/\D/g, '');
      if (cleaned.length >= 2) {
        cleaned = cleaned.slice(0, 4);
        cleaned = `${cleaned.slice(0, 2)}/${cleaned.slice(2)}`;
      }
      setFormData({ ...formData, [name]: cleaned });
    } else if (name === 'cvv') {
      setFormData({ ...formData, [name]: value.replaceAll(/\D/g, '').slice(0, 3) });
    } else {
      setFormData({ ...formData, [name]: value });
    }
  };

  const validateForm = () => {
    if (paymentMethod === 'card') {
      if (!formData.cardholderName.trim()) { alert('Please enter cardholder name'); return false; }
      if (!formData.cardNumber.split(' ').join('').match(/^\d{16}$/)) { alert('Please enter a valid 16-digit card number'); return false; }
      if (!formData.expiryDate.match(/^\d{2}\/\d{2}$/)) { alert('Please enter expiry date in MM/YY format'); return false; }
      if (!formData.cvv.match(/^\d{3}$/)) { alert('Please enter a valid 3-digit CVV'); return false; }
    } else if (paymentMethod === 'ewallet') {
      if (!ewalletNumber.trim()) { alert('Please enter your e-wallet number'); return false; }
    }
    return true;
  };

  const convertTimeTo24Hour = (time12h) => {
    const [time, period] = time12h.split(' ');
    let [hours, minutes] = time.split(':');
    if (period === 'PM' && hours !== '12') hours = String(Number.parseInt(hours) + 12);
    else if (period === 'AM' && hours === '12') hours = '00';
    return `${hours}:${minutes}`;
  };

  const handlePayment = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;
    setLoading(true);
    try {
      const appointmentData = await apiPost('/api/appointments/book?clientId=' + bookingData.clientId, {
        artistId: bookingData.artistId,
        serviceId: bookingData.service.id,
        appointmentDate: `${bookingData.date}T${convertTimeTo24Hour(bookingData.time)}:00`,
        notes: bookingData.notes || ''
      });
      if (appointmentData.success) {
        const ref = 'BOOKING-' + appointmentData.data.id + '-' + Date.now().toString().slice(-5);
        setBookingRef(ref);
        setPaymentStep('success');
      } else {
        setPaymentStep('error');
      }
    } catch (error) {
      console.error('Payment error:', error);
      setPaymentStep('error');
    } finally {
      setLoading(false);
    }
  };

  const handleGoToDashboard = () => { navigate('/dashboard'); };

  if (paymentStep === 'success') {
    return (
      <div className="payment-container">
        <div className="payment-content">
          <div className="success-section">
            <div className="success-icon">
              <svg viewBox="0 0 24 24" width="80" height="80" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10"/><path d="M9 12l2 2 4-4"/>
              </svg>
            </div>
            <h2>Booking Confirmed!</h2>
            <p className="booking-ref">Booking Ref: {bookingRef}</p>
            <div className="booking-details">
              <h3>Booking Details</h3>
              <div className="detail-item"><span>Service:</span><strong>{bookingData.service?.name}</strong></div>
              <div className="detail-item"><span>Date &amp; Time:</span><strong>{new Date(bookingData.date).toLocaleDateString()} at {bookingData.time}</strong></div>
              <div className="detail-item"><span>Artist:</span><strong>{bookingData.service?.artistName}</strong></div>
              <div className="detail-item"><span>Payment:</span><strong>{PAYMENT_METHODS.find(m => m.key === paymentMethod)?.label}</strong></div>
              <div className="detail-item amount"><span>Amount:</span><strong>₱{totalAmount}</strong></div>
            </div>
            <button className="btn-success" onClick={handleGoToDashboard}>Go to Dashboard</button>
          </div>
        </div>
      </div>
    );
  }

  if (paymentStep === 'error') {
    return (
      <div className="payment-container">
        <div className="payment-content">
          <div className="error-section">
            <div className="error-icon">✕</div>
            <h2>Booking Failed</h2>
            <p>Something went wrong. Please try again.</p>
            <button className="btn-primary" onClick={() => setPaymentStep('form')}>Try Again</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="payment-container">
      <div className="payment-content">

        {/* Left: Payment Form */}
        <div className="payment-form-section">
          <h2>Payment Details</h2>

          {/* Method selector */}
          <div className="pm-method-tabs">
            {PAYMENT_METHODS.map(m => (
              <button
                key={m.key}
                type="button"
                className={`pm-method-tab${paymentMethod === m.key ? ' active' : ''}`}
                onClick={() => setPaymentMethod(m.key)}
              >
                <span className="pm-method-icon">{m.icon}</span>
                <span className="pm-method-label">{m.label}</span>
              </button>
            ))}
          </div>

          <form onSubmit={handlePayment}>

            {/* Card fields */}
            {paymentMethod === 'card' && (
              <>
                <div className="form-group">
                  <label htmlFor="cardholderName">Cardholder Name</label>
                  <input id="cardholderName" type="text" name="cardholderName" placeholder="Enter your full name"
                    value={formData.cardholderName} onChange={handleInputChange} disabled={loading} />
                </div>
                <div className="form-group">
                  <label htmlFor="cardNumber">Card Number</label>
                  <input id="cardNumber" type="text" name="cardNumber" placeholder="1234 5678 9012 3456"
                    value={formData.cardNumber} onChange={handleInputChange} maxLength="19" disabled={loading} />
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="expiryDate">Expiry Date</label>
                    <input id="expiryDate" type="text" name="expiryDate" placeholder="MM/YY"
                      value={formData.expiryDate} onChange={handleInputChange} maxLength="5" disabled={loading} />
                  </div>
                  <div className="form-group">
                    <label htmlFor="cvv">CVV</label>
                    <input id="cvv" type="text" name="cvv" placeholder="123"
                      value={formData.cvv} onChange={handleInputChange} maxLength="3" disabled={loading} />
                  </div>
                </div>
              </>
            )}

            {/* E-wallet fields */}
            {paymentMethod === 'ewallet' && (
              <>
                <div className="form-group">
                  <label htmlFor="ewalletType">E-Wallet Provider</label>
                  <div className="pm-ewallet-grid">
                    {EWALLETS.map(w => (
                      <button
                        key={w}
                        type="button"
                        className={`pm-ewallet-btn${ewalletType === w ? ' active' : ''}`}
                        onClick={() => setEwalletType(w)}
                      >{w}</button>
                    ))}
                  </div>
                </div>
                <div className="form-group">
                  <label htmlFor="ewalletNumber">Registered Number</label>
                  <input id="ewalletNumber" type="text" placeholder="09XX XXX XXXX"
                    value={ewalletNumber} onChange={e => setEwalletNumber(e.target.value)} disabled={loading} />
                </div>
                <div className="pm-walkin-note">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>
                  You will receive a payment request to your {ewalletType} account.
                </div>
              </>
            )}

            {/* Walk-in */}
            {paymentMethod === 'walkin' && (
              <div className="pm-walkin-info">
                <div className="pm-walkin-icon">
                  <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                  </svg>
                </div>
                <h3>Pay at the Venue</h3>
                <p>Your appointment will be confirmed. Please pay the artist directly when you arrive.</p>
                <div className="pm-walkin-amount">Amount due: <strong>₱{totalAmount}</strong></div>
                <ul className="pm-walkin-list">
                  <li>Cash or card accepted at the venue</li>
                  <li>Bring this booking reference</li>
                  <li>Arrive 5 minutes before your appointment</li>
                </ul>
              </div>
            )}

            <button type="submit" className="btn-pay" disabled={loading}>
              {loading ? 'Processing…' : (paymentMethod === 'walkin' ? 'Confirm Booking' : 'Pay Now')}
            </button>
          </form>

          <p className="security-note">🔒 Your information is secure and encrypted</p>
        </div>

        {/* Right: Order Summary */}
        <div className="order-summary-section">
          <h3>Order Summary</h3>
          {bookingData.service && (
            <>
              <div className="order-item">
                {bookingData.service.photos?.[0] ? (
                  <img src={bookingData.service.photos[0]} alt={bookingData.service.name} className="item-photo" />
                ) : (
                  <div className="item-icon-fallback">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.9)" strokeWidth="1.5">
                      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                    </svg>
                  </div>
                )}
                <div className="item-details">
                  <h4>{bookingData.service.name}</h4>
                  <p className="by-artist">by {bookingData.service.artistName}</p>
                </div>
              </div>
              <div className="order-info">
                <div className="info-row"><span>Date:</span>
                  <span>{new Date(bookingData.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span>
                </div>
                <div className="info-row"><span>Time:</span><span>{bookingData.time}</span></div>
                <div className="info-row"><span>Payment:</span>
                  <span>{PAYMENT_METHODS.find(m => m.key === paymentMethod)?.label}</span>
                </div>
              </div>
              <div className="total-section">
                <span className="total-label">Total Amount</span>
                <span className="total-amount">₱{totalAmount}</span>
              </div>
            </>
          )}
        </div>

      </div>
    </div>
  );
};

export default PaymentPage;

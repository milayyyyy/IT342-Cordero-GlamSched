import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import '../styles/PaymentPage.css';
import { apiPost } from '../utils/api';

const PaymentPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const bookingData = location.state || {};

  const [paymentStep, setPaymentStep] = useState('form'); // 'form' or 'success' or 'error'
  const [formData, setFormData] = useState({
    cardholderName: '',
    cardNumber: '',
    expiryDate: '',
    cvv: ''
  });
  const [loading, setLoading] = useState(false);
  const [bookingRef, setBookingRef] = useState('');

  const totalAmount = bookingData.service ? (parseFloat(bookingData.service.price) * 1.1).toFixed(2) : '0.00';

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    
    // Format card number
    if (name === 'cardNumber') {
      const cleaned = value.replace(/\D/g, '');
      const formatted = cleaned.slice(0, 16).replace(/(\d{4})/g, '$1 ').trim();
      setFormData({ ...formData, [name]: formatted });
    }
    // Format expiry date
    else if (name === 'expiryDate') {
      let cleaned = value.replace(/\D/g, '');
      if (cleaned.length >= 2) {
        cleaned = cleaned.slice(0, 4);
        cleaned = cleaned.slice(0, 2) + '/' + cleaned.slice(2);
      }
      setFormData({ ...formData, [name]: cleaned });
    }
    // Format CVV
    else if (name === 'cvv') {
      setFormData({ ...formData, [name]: value.replace(/\D/g, '').slice(0, 3) });
    }
    else {
      setFormData({ ...formData, [name]: value });
    }
  };

  const validateForm = () => {
    if (!formData.cardholderName.trim()) {
      alert('Please enter cardholder name');
      return false;
    }
    if (!formData.cardNumber.replace(/\s/g, '').match(/^\d{16}$/)) {
      alert('Please enter a valid 16-digit card number');
      return false;
    }
    if (!formData.expiryDate.match(/^\d{2}\/\d{2}$/)) {
      alert('Please enter expiry date in MM/YY format');
      return false;
    }
    if (!formData.cvv.match(/^\d{3}$/)) {
      alert('Please enter a valid 3-digit CVV');
      return false;
    }
    return true;
  };

  const handlePayment = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    try {
      // Create appointment booking
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

  const convertTimeTo24Hour = (time12h) => {
    const [time, period] = time12h.split(' ');
    let [hours, minutes] = time.split(':');
    
    if (period === 'PM' && hours !== '12') {
      hours = String(parseInt(hours) + 12);
    } else if (period === 'AM' && hours === '12') {
      hours = '00';
    }
    
    return `${hours}:${minutes}`;
  };

  const handleGoToDashboard = () => {
    navigate('/dashboard');
  };

  if (paymentStep === 'success') {
    return (
      <div className="payment-container">
        <div className="payment-content">
          <div className="success-section">
            <div className="success-icon">
              <svg viewBox="0 0 24 24" width="80" height="80" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <path d="M9 12l2 2 4-4" />
              </svg>
            </div>
            <h2>Payment Successful</h2>
            <p className="booking-ref">Booking Ref: {bookingRef}</p>
            <div className="booking-details">
              <h3>Booking Confirmed</h3>
              <div className="detail-item">
                <span>Service:</span>
                <strong>{bookingData.service?.name}</strong>
              </div>
              <div className="detail-item">
                <span>Date & Time:</span>
                <strong>{new Date(bookingData.date).toLocaleDateString()} at {bookingData.time}</strong>
              </div>
              <div className="detail-item">
                <span>Artist:</span>
                <strong>{bookingData.service?.artistName}</strong>
              </div>
              <div className="detail-item amount">
                <span>Amount Paid:</span>
                <strong>${totalAmount}</strong>
              </div>
            </div>
            <button className="btn-success" onClick={handleGoToDashboard}>
              Go to Dashboard
            </button>
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
            <h2>Payment Failed</h2>
            <p>Something went wrong with your payment. Please try again.</p>
            <button className="btn-primary" onClick={() => setPaymentStep('form')}>
              Try Again
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="payment-container">
      <div className="payment-content">
        {/* Left Section: Payment Form */}
        <div className="payment-form-section">
          <h2>Payment Details</h2>
          <form onSubmit={handlePayment}>
            <div className="form-group">
              <label>Cardholder Name</label>
              <input
                type="text"
                name="cardholderName"
                placeholder="Enter your full name"
                value={formData.cardholderName}
                onChange={handleInputChange}
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label>Card Number</label>
              <input
                type="text"
                name="cardNumber"
                placeholder="1234 5678 9012 3456"
                value={formData.cardNumber}
                onChange={handleInputChange}
                maxLength="19"
                disabled={loading}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Expiry Date</label>
                <input
                  type="text"
                  name="expiryDate"
                  placeholder="MM/YY"
                  value={formData.expiryDate}
                  onChange={handleInputChange}
                  maxLength="5"
                  disabled={loading}
                />
              </div>
              <div className="form-group">
                <label>CVV</label>
                <input
                  type="text"
                  name="cvv"
                  placeholder="123"
                  value={formData.cvv}
                  onChange={handleInputChange}
                  maxLength="3"
                  disabled={loading}
                />
              </div>
            </div>

            <button type="submit" className="btn-pay" disabled={loading}>
              {loading ? 'Processing...' : 'Pay Now'}
            </button>
          </form>

          <p className="security-note">
            🔒 Your payment information is secure and encrypted
          </p>
        </div>

        {/* Right Section: Order Summary */}
        <div className="order-summary-section">
          <h3>Order Summary</h3>
          
          {bookingData.service && (
            <>
              <div className="order-item">
                <div className="item-icon">💄</div>
                <div className="item-details">
                  <h4>{bookingData.service.name}</h4>
                  <p className="by-artist">by {bookingData.service.artistName}</p>
                </div>
              </div>

              <div className="order-info">
                <div className="info-row">
                  <span>Service:</span>
                  <span>{bookingData.service.name}</span>
                </div>
                <div className="info-row">
                  <span>Date:</span>
                  <span>{new Date(bookingData.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span>
                </div>
                <div className="info-row">
                  <span>Time:</span>
                  <span>{bookingData.time}</span>
                </div>
                <div className="info-row">
                  <span>Duration:</span>
                  <span>{bookingData.service.duration} minutes</span>
                </div>
              </div>

              <div className="order-calculation">
                <div className="calc-row">
                  <span>Service Price:</span>
                  <span>${bookingData.service.price}</span>
                </div>
                <div className="calc-row">
                  <span>Tax (10%):</span>
                  <span>${(bookingData.service.price * 0.1).toFixed(2)}</span>
                </div>
                <div className="calc-row divider"></div>
              </div>

              <div className="total-section">
                <span className="total-label">Total Amount</span>
                <span className="total-amount">${totalAmount}</span>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default PaymentPage;

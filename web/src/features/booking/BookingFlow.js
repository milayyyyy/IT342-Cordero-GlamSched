import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './BookingFlow.css';
import { apiGet } from '../../shared/api';

const AlertModal = ({ message, onClose }) => (
  <div className="bf-modal-overlay" onClick={onClose}>
    <div className="bf-modal" onClick={e => e.stopPropagation()}>
      <div className="bf-modal-icon">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#d4af37" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
      </div>
      <p className="bf-modal-message">{message}</p>
      <button className="bf-modal-btn" onClick={onClose}>OK</button>
    </div>
  </div>
);

const BookingFlow = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(1);
  const [modalMessage, setModalMessage] = useState(null);

  const showAlert = (msg) => setModalMessage(msg);
  const [selectedService, setSelectedService] = useState(null);
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedTime, setSelectedTime] = useState(null);
  const [notes, setNotes] = useState('');
  const [artistId, setArtistId] = useState(null);
  const [loading, setLoading] = useState(true);

  const userId = localStorage.getItem('userId');
  const timeSlots = [
    '09:00 AM', '10:00 AM', '11:00 AM', '12:00 PM',
    '01:00 PM', '02:00 PM', '03:00 PM', '04:00 PM',
    '05:00 PM', '06:00 PM'
  ];

  useEffect(() => {
    fetchServices();
  }, []);

  const fetchServices = async () => {
    try {
      const data = await apiGet('/api/services');
      const list = Array.isArray(data) ? data : (data.data || []);
      if (list && list.length > 0) setArtistId(list[0].artistId);

      // Auto-select service passed from dashboard
      const stored = localStorage.getItem('selectedService');
      if (stored) {
        try {
          const preSelected = JSON.parse(stored);
          // Match by id against the freshly-loaded list so we get up-to-date data
          const match = list.find(s => s.id === preSelected.id) || preSelected;
          setSelectedService(match);
          if (match.artistId) setArtistId(match.artistId);
        } catch (_) {}
        localStorage.removeItem('selectedService');
      }
    } catch (error) {
      console.error('Error fetching services:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleNext = () => {
    if (currentStep === 1 && !selectedService) {
      showAlert('Please select a service before continuing');
      return;
    }
    if (currentStep === 1 && !selectedDate) {
      showAlert('Please select a date');
      return;
    }
    if (currentStep === 2 && !selectedTime) {
      showAlert('Please select a time');
      return;
    }
    if (currentStep < 3) {
      setCurrentStep(currentStep + 1);
    }
  };

  const handleBack = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const handleProceedToPayment = () => {
    if (!selectedService || !selectedDate || !selectedTime) {
      showAlert('Please complete all booking steps');
      return;
    }
    navigate('/payment', {
      state: {
        service: selectedService,
        date: selectedDate,
        time: selectedTime,
        notes: notes,
        artistId: artistId,
        clientId: userId
      }
    });
  };

  const getMinDate = () => {
    const today = new Date();
    return today.toISOString().split('T')[0];
  };

  if (loading) {
    return <div className="booking-flow-container"><p>Loading services...</p></div>;
  }

  return (
    <div className="booking-flow-container">
      {modalMessage && <AlertModal message={modalMessage} onClose={() => setModalMessage(null)} />}
      <div className="booking-content">
        <div className="left-section">
          {/* Step Indicators */}
          <div className="step-indicators">
            {[1, 2, 3].map((step) => (
              <div key={step} className={`step ${currentStep >= step ? 'active' : ''}`}>
                <div className={`step-number ${currentStep > step ? 'completed' : ''}`}>
                  {currentStep > step ? '✓' : step}
                </div>
                <div className="step-label">
                  {step === 1 ? 'Date' : step === 2 ? 'Time' : 'Confirmation'}
                </div>
              </div>
            ))}
          </div>

          {/* Step 1: Date Selection */}
          {currentStep === 1 && (
            <div className="booking-step">
              <h2>Select Date</h2>
              <div className="selected-service-mini">
                <div className="service-info-mini">
                  <p className="service-name">{selectedService?.name}</p>
                  <p className="service-artist-mini">{selectedService?.artistName}</p>
                </div>
                <p className="service-price-mini">₱{selectedService?.price}</p>
              </div>
              <div className="date-picker">
                <input
                  type="date"
                  value={selectedDate || ''}
                  onChange={(e) => setSelectedDate(e.target.value)}
                  min={getMinDate()}
                  className="date-input"
                />
              </div>
              {selectedDate && (
                <div className="selected-date-display">
                  <p>Selected: <strong>{new Date(selectedDate).toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</strong></p>
                </div>
              )}
            </div>
          )}

          {/* Step 2: Time Selection */}
          {currentStep === 2 && (
            <div className="booking-step">
              <h2>Select Time</h2>
              <div className="selected-service-mini">
                <div className="service-info-mini">
                  <p className="service-name">{selectedService?.name}</p>
                  <p className="service-date-mini">{new Date(selectedDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}</p>
                </div>
              </div>
              <div className="time-slots">
                {timeSlots.map((time) => (
                  <button
                    key={time}
                    className={`time-slot ${selectedTime === time ? 'selected' : ''}`}
                    onClick={() => setSelectedTime(time)}
                  >
                    {time}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Step 3: Confirmation */}
          {currentStep === 3 && (
            <div className="booking-step">
              <h2>Add Notes (Optional)</h2>
              <div className="booking-summary">
                <div className="summary-item">
                  <span className="label">Service:</span>
                  <span className="value">{selectedService?.name}</span>
                </div>
                <div className="summary-item">
                  <span className="label">Artist:</span>
                  <span className="value">{selectedService?.artistName}</span>
                </div>
                <div className="summary-item">
                  <span className="label">Date:</span>
                  <span className="value">{new Date(selectedDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span>
                </div>
                <div className="summary-item">
                  <span className="label">Time:</span>
                  <span className="value">{selectedTime}</span>
                </div>
                <div className="summary-item divider">
                  <span className="label">Amount:</span>
                  <span className="value price">₱{selectedService?.price}</span>
                </div>
              </div>
              <textarea
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Add any special requests or notes..."
                className="notes-textarea"
              />
            </div>
          )}

          {/* Navigation Buttons */}
          <div className="booking-buttons">
            {currentStep > 1 && (
              <button className="btn-secondary" onClick={handleBack}>
                Back
              </button>
            )}
            {currentStep < 3 ? (
              <button className="btn-primary" onClick={handleNext}>
                Next
              </button>
            ) : (
              <button className="btn-primary" onClick={handleProceedToPayment}>
                Proceed to Payment
              </button>
            )}
          </div>
        </div>

        {/* Right Section: Order Summary */}
        <div className="right-section">
          <div className="order-summary">
            <h3>ORDER SUMMARY</h3>
            {selectedService && (
              <>
                <div className="summary-card">
                  <div className="service-box">
                    {selectedService.photos?.[0] ? (
                      <img src={selectedService.photos[0]} alt={selectedService.name} className="summary-service-photo" />
                    ) : (
                      <div className="summary-service-icon">
                        <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.9)" strokeWidth="1.5">
                          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                        </svg>
                      </div>
                    )}
                    <div>
                      <h4>{selectedService.name}</h4>
                      <p className="by-artist">by {selectedService.artistName}</p>
                    </div>
                  </div>
                </div>

                <div className="summary-details">
                  {selectedDate && (
                    <div className="detail-row">
                      <span>Date:</span>
                      <span>{new Date(selectedDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}</span>
                    </div>
                  )}
                  {selectedTime && (
                    <div className="detail-row">
                      <span>Time:</span>
                      <span>{selectedTime}</span>
                    </div>
                  )}
                </div>

                <div className="summary-total">
                  <div className="total-row">
                    <span>Subtotal:</span>
                    <span>₱{selectedService.price}</span>
                  </div>
                  <div className="total-amount">
                    <span>TOTAL</span>
                    <span>₱{parseFloat(selectedService.price).toFixed(2)}</span>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookingFlow;

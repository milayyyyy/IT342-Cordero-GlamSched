import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/BookingFlow.css';
import { apiGet } from '../utils/api';

const BookingFlow = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(1);
  const [services, setServices] = useState([]);
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
      setServices(list || []);
      if (list && list.length > 0) setArtistId(list[0].artistId);
    } catch (error) {
      console.error('Error fetching services:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleServiceSelect = (service) => {
    setSelectedService(service);
    setArtistId(service.artistId);
  };

  const handleNext = () => {
    if (currentStep === 1 && !selectedService) {
      alert('Please select a service');
      return;
    }
    if (currentStep === 2 && !selectedDate) {
      alert('Please select a date');
      return;
    }
    if (currentStep === 3 && !selectedTime) {
      alert('Please select a time');
      return;
    }
    if (currentStep < 4) {
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
      alert('Please complete all booking steps');
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
      <div className="booking-content">
        <div className="left-section">
          {/* Step Indicators */}
          <div className="step-indicators">
            {[1, 2, 3, 4].map((step) => (
              <div key={step} className={`step ${currentStep >= step ? 'active' : ''}`}>
                <div className={`step-number ${currentStep >= step ? 'completed' : ''}`}>
                  {step}
                </div>
                <div className="step-label">
                  {step === 1 ? 'Service' : step === 2 ? 'Date' : step === 3 ? 'Time' : 'Confirmation'}
                </div>
              </div>
            ))}
          </div>

          {/* Step 1: Service Selection */}
          {currentStep === 1 && (
            <div className="booking-step">
              <h2>Select a Service</h2>
              <div className="services-grid">
                {services.map((service) => (
                  <div
                    key={service.id}
                    className={`service-card ${selectedService?.id === service.id ? 'selected' : ''}`}
                    onClick={() => handleServiceSelect(service)}
                  >
                    <div className="service-icon">💄</div>
                    <h3>{service.name}</h3>
                    <p className="service-artist">by {service.artistName}</p>
                    <p className="service-description">{service.description}</p>
                    <div className="service-details">
                      <span className="service-price">${service.price}</span>
                      <span className="service-duration">{service.duration} min</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Step 2: Date Selection */}
          {currentStep === 2 && (
            <div className="booking-step">
              <h2>Select Date</h2>
              <div className="selected-service-mini">
                <div className="service-icon-mini">💄</div>
                <div className="service-info-mini">
                  <p className="service-name">{selectedService?.name}</p>
                  <p className="service-artist-mini">{selectedService?.artistName}</p>
                </div>
                <p className="service-price-mini">${selectedService?.price}</p>
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

          {/* Step 3: Time Selection */}
          {currentStep === 3 && (
            <div className="booking-step">
              <h2>Select Time</h2>
              <div className="selected-service-mini">
                <div className="service-icon-mini">💄</div>
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

          {/* Step 4: Confirmation */}
          {currentStep === 4 && (
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
                  <span className="value price">${selectedService?.price}</span>
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
            {currentStep < 4 ? (
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
                    <div className="service-icon-large">💄</div>
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
                  <div className="detail-row">
                    <span>Duration:</span>
                    <span>{selectedService.duration} minutes</span>
                  </div>
                </div>

                <div className="summary-total">
                  <div className="total-row">
                    <span>Subtotal:</span>
                    <span>${selectedService.price}</span>
                  </div>
                  <div className="total-row">
                    <span>Tax:</span>
                    <span>${(selectedService.price * 0.1).toFixed(2)}</span>
                  </div>
                  <div className="total-amount">
                    <span>TOTAL</span>
                    <span>${(selectedService.price * 1.1).toFixed(2)}</span>
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

import React, { useState, useEffect } from 'react';
import '../styles/BookAppointment.css';
import { apiGet, apiPost } from '../utils/api';

function BookAppointment() {
  const [services, setServices] = useState([]);
  const [artists, setArtists] = useState([]);
  const [formData, setFormData] = useState({
    artistId: '',
    serviceId: '',
    appointmentDate: '',
    appointmentTime: '',
    notes: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    fetchServices();
  }, []);

  const fetchServices = async () => {
    try {
      const data = await apiGet('/api/services');
      const list = Array.isArray(data) ? data : (data.data || []);
      setServices(list);
      // Extract unique artists
      const uniqueArtists = [...new Map(list.map(s => [s.artistId, { id: s.artistId, name: s.artistName }])).values()];
      setArtists(uniqueArtists);
    } catch (err) {
      console.error('Error fetching services:', err);
      setError(err.message || 'Failed to load services');
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validation
    if (!formData.artistId || !formData.serviceId || !formData.appointmentDate || !formData.appointmentTime) {
      setError('Please fill in all required fields');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Combine date and time
      const appointmentDateTime = `${formData.appointmentDate}T${formData.appointmentTime}`;
      
      const requestBody = {
        artistId: parseInt(formData.artistId),
        serviceId: parseInt(formData.serviceId),
        appointmentDate: appointmentDateTime,
        notes: formData.notes
      };

      // Get client ID from localStorage (set during login)
      const clientId = localStorage.getItem('userId');

      const data = await apiPost(`/api/appointments/book?clientId=${clientId}`, requestBody);

      if (data.success) {
        setSuccess(true);
        setFormData({
          artistId: '',
          serviceId: '',
          appointmentDate: '',
          appointmentTime: '',
          notes: ''
        });
        setTimeout(() => setSuccess(false), 5000);
      } else {
        setError(data.error || 'Failed to book appointment');
      }
    } catch (err) {
      console.error('Error booking appointment:', err);
      setError('Error connecting to server');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="book-appointment-container">
      <h2>Book an Appointment</h2>
      <p className="form-subtitle">Schedule a new beauty session</p>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">Appointment booked successfully! ✓</div>}

      <form onSubmit={handleSubmit} className="booking-form">
        <div className="form-group">
          <label htmlFor="artist">Select Artist *</label>
          <select
            id="artist"
            name="artistId"
            value={formData.artistId}
            onChange={handleInputChange}
            required
          >
            <option value="">Choose an artist...</option>
            {artists.map(artist => (
              <option key={artist.id} value={artist.id}>{artist.name}</option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="service">Select Service *</label>
          <select
            id="service"
            name="serviceId"
            value={formData.serviceId}
            onChange={handleInputChange}
            required
          >
            <option value="">Choose a service...</option>
            {services.map(service => (
              <option key={service.id} value={service.id}>
                {service.name} - ${service.price.toFixed(2)} ({service.duration} mins)
              </option>
            ))}
          </select>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="date">Date *</label>
            <input
              type="date"
              id="date"
              name="appointmentDate"
              value={formData.appointmentDate}
              onChange={handleInputChange}
              required
              min={new Date().toISOString().split('T')[0]}
            />
          </div>

          <div className="form-group">
            <label htmlFor="time">Time *</label>
            <input
              type="time"
              id="time"
              name="appointmentTime"
              value={formData.appointmentTime}
              onChange={handleInputChange}
              required
            />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="notes">Additional Notes</label>
          <textarea
            id="notes"
            name="notes"
            value={formData.notes}
            onChange={handleInputChange}
            placeholder="Any special requests or notes?"
            rows="4"
          />
        </div>

        <button 
          type="submit" 
          className="submit-btn"
          disabled={loading}
        >
          {loading ? 'Booking...' : 'Book Appointment'}
        </button>
      </form>
    </div>
  );
}

export default BookAppointment;

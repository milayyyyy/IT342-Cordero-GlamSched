import React, { useState, useEffect } from 'react';
import '../styles/Services.css';
import { apiGet } from '../utils/api';

function BrowseServices() {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedService, setSelectedService] = useState(null);

  useEffect(() => {
    fetchServices();
  }, []);

  const fetchServices = async () => {
    try {
      setLoading(true);
      const data = await apiGet('/api/services');
      const list = Array.isArray(data) ? data : (data.data || []);
      setServices(list);
    } catch (err) {
      setError(err.message || 'Error connecting to server');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleServiceClick = (service) => {
    setSelectedService(service);
  };

  const handleCloseModal = () => {
    setSelectedService(null);
  };

  if (loading) {
    return (
      <div className="services-container">
        <h2>Browse Services</h2>
        <p>Loading services...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="services-container">
        <h2>Browse Services</h2>
        <p className="error-message">{error}</p>
        <button onClick={fetchServices}>Try Again</button>
      </div>
    );
  }

  return (
    <div className="services-container">
      <h2>Browse Services</h2>
      <p className="services-subtitle">Explore available beauty services</p>
      
      {services.length === 0 ? (
        <div className="empty-state">
          <p>No services available yet</p>
        </div>
      ) : (
        <div className="services-grid">
          {services.map(service => (
            <div 
              key={service.id} 
              className="service-card"
              onClick={() => handleServiceClick(service)}
            >
              <div className="service-header">
                <h3>{service.name}</h3>
                <span className="service-price">${service.price.toFixed(2)}</span>
              </div>
              <p className="service-description">{service.description}</p>
              <div className="service-info">
                <span className="service-duration">⏱ {service.duration} mins</span>
                <span className="service-artist">👨‍🎨 {service.artistName}</span>
              </div>
              <button className="service-btn">View Details</button>
            </div>
          ))}
        </div>
      )}

      {selectedService && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <button className="modal-close" onClick={handleCloseModal}>✕</button>
            <h3>{selectedService.name}</h3>
            <p className="service-description">{selectedService.description}</p>
            <div className="modal-details">
              <div className="detail-item">
                <span className="label">Price:</span>
                <span className="value">${selectedService.price.toFixed(2)}</span>
              </div>
              <div className="detail-item">
                <span className="label">Duration:</span>
                <span className="value">{selectedService.duration} minutes</span>
              </div>
              <div className="detail-item">
                <span className="label">Artist:</span>
                <span className="value">{selectedService.artistName}</span>
              </div>
            </div>
            <button className="book-btn">Book This Service</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default BrowseServices;

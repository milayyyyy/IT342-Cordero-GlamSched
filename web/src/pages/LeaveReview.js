import React, { useState, useEffect } from 'react';
import '../styles/LeaveReview.css';
import { apiGet, apiPost } from '../utils/api';

function LeaveReview() {
  const [appointments, setAppointments] = useState([]);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [formData, setFormData] = useState({
    rating: 5,
    comment: ''
  });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    fetchCompletedAppointments();
  }, []);

  const fetchCompletedAppointments = async () => {
    try {
      setLoading(true);
      const clientId = localStorage.getItem('userId');
      
      const data = await apiGet(`/api/appointments/client/${clientId}`);
      const list = Array.isArray(data) ? data : (data.data || []);
      const completedAppointments = list.filter(apt => apt.status === 'COMPLETED');
      setAppointments(completedAppointments);
    } catch (err) {
      console.error('Error fetching appointments:', err);
      setError('Error connecting to server');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAppointment = (appointment) => {
    setSelectedAppointment(appointment);
    setFormData({ rating: 5, comment: '' });
  };

  const handleRatingClick = (rating) => {
    setFormData(prev => ({ ...prev, rating }));
  };

  const handleCommentChange = (e) => {
    setFormData(prev => ({ ...prev, comment: e.target.value }));
  };

  const handleSubmitReview = async (e) => {
    e.preventDefault();

    if (!selectedAppointment) {
      setError('Please select an appointment');
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const clientId = localStorage.getItem('userId');
      const reviewRequest = {
        appointmentId: selectedAppointment.id,
        artistId: selectedAppointment.artistId,
        rating: parseInt(formData.rating),
        comment: formData.comment
      };

      const data = await apiPost(`/api/reviews/create?reviewerId=${clientId}`, reviewRequest);

      if (data.success) {
        setSuccess(true);
        setSelectedAppointment(null);
        setFormData({ rating: 5, comment: '' });
        setTimeout(() => {
          setSuccess(false);
          fetchCompletedAppointments();
        }, 3000);
      } else {
        setError(data.error || 'Failed to submit review');
      }
    } catch (err) {
      console.error('Error submitting review:', err);
      setError('Error connecting to server');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="review-container">
        <h2>Leave a Review</h2>
        <p>Loading appointments...</p>
      </div>
    );
  }

  return (
    <div className="review-container">
      <h2>Leave a Review</h2>
      <p className="review-subtitle">Share your beauty experience</p>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">Review submitted successfully! ✓</div>}

      {appointments.length === 0 ? (
        <div className="empty-state">
          <p>You have no completed appointments yet</p>
        </div>
      ) : !selectedAppointment ? (
        <div className="appointments-list">
          <h3>Your Completed Appointments</h3>
          {appointments.map(appointment => (
            <div key={appointment.id} className="appointment-item">
              <div className="appointment-info">
                <h4>{appointment.serviceName}</h4>
                <p className="artist-name">👨‍🎨 {appointment.artistName}</p>
                <p className="appointment-date">
                  📅 {new Date(appointment.appointmentDate).toLocaleDateString()} at{' '}
                  {new Date(appointment.appointmentDate).toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit'
                  })}
                </p>
              </div>
              <button 
                className="review-btn"
                onClick={() => handleSelectAppointment(appointment)}
              >
                Write Review
              </button>
            </div>
          ))}
        </div>
      ) : (
        <form onSubmit={handleSubmitReview} className="review-form">
          <div className="selected-appointment">
            <h3>{selectedAppointment.serviceName}</h3>
            <p>By {selectedAppointment.artistName}</p>
          </div>

          <div className="form-group">
            <label>Rating *</label>
            <div className="rating-selector">
              {[1, 2, 3, 4, 5].map(star => (
                <button
                  key={star}
                  type="button"
                  className={`star ${formData.rating >= star ? 'active' : ''}`}
                  onClick={() => handleRatingClick(star)}
                  title={`${star} star${star !== 1 ? 's' : ''}`}
                >
                  ⭐
                </button>
              ))}
            </div>
            <p className="rating-display">{formData.rating} out of 5 stars</p>
          </div>

          <div className="form-group">
            <label htmlFor="comment">Your Review</label>
            <textarea
              id="comment"
              value={formData.comment}
              onChange={handleCommentChange}
              placeholder="Tell us about your experience..."
              rows="5"
              maxLength="1000"
            />
            <p className="char-count">{formData.comment.length}/1000 characters</p>
          </div>

          <div className="button-group">
            <button 
              type="button"
              className="cancel-btn"
              onClick={() => setSelectedAppointment(null)}
            >
              Cancel
            </button>
            <button 
              type="submit"
              className="submit-btn"
              disabled={submitting}
            >
              {submitting ? 'Submitting...' : 'Submit Review'}
            </button>
          </div>
        </form>
      )}
    </div>
  );
}

export default LeaveReview;

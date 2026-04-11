import React, { useState } from 'react';
import { apiPost } from '../utils/api';
import '../styles/AddServiceModal.css';

function AddServiceModal({ isOpen, onClose, onServiceAdded, artistId }) {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: ''
  });
  const [photos, setPhotos] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handlePhotoSelect = async (e) => {
    const files = Array.from(e.target.files || []);
    
    if (photos.length + files.length > 10) {
      setError(`Maximum 10 photos allowed. You already have ${photos.length} photo(s).`);
      return;
    }

    setError('');

    for (const file of files) {
      // Validate file type
      if (!file.type.startsWith('image/')) {
        setError('Please select only image files');
        return;
      }

      // Validate file size (max 2MB per image)
      if (file.size > 2 * 1024 * 1024) {
        setError('Each image must be less than 2MB');
        return;
      }

      // Read file as base64
      const reader = new FileReader();
      reader.onloadend = () => {
        setPhotos(prev => [...prev, {
          id: Date.now() + Math.random(),
          data: reader.result,
          name: file.name
        }]);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleRemovePhoto = (photoId) => {
    setPhotos(photos.filter(p => p.id !== photoId));
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.currentTarget.classList.add('drag-over');
  };

  const handleDragLeave = (e) => {
    e.currentTarget.classList.remove('drag-over');
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.currentTarget.classList.remove('drag-over');
    
    const input = document.createElement('input');
    input.type = 'file';
    input.multiple = true;
    Object.defineProperty(input, 'files', {
      value: e.dataTransfer.files
    });
    handlePhotoSelect({ target: input });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validation
    if (!formData.name.trim()) {
      setError('Service name is required');
      return;
    }
    if (!formData.price || parseFloat(formData.price) <= 0) {
      setError('Please enter a valid price');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await apiPost(`/api/services/create?artistId=${artistId}`, {
        name: formData.name.trim(),
        description: formData.description.trim() || '',
        price: parseFloat(formData.price) || 0,
        duration: 60, // Default 60 minutes
        photos: photos.map(p => p.data) // Array of base64 strings
      });

      if (response?.success || response?.data) {
        // Reset form
        setFormData({
          name: '',
          description: '',
          price: ''
        });
        setPhotos([]);
        // Notify parent to refresh services
        onServiceAdded();
        onClose();
      } else {
        setError(response?.error || 'Failed to create service');
      }
    } catch (err) {
      setError('Error creating service: ' + (err.message || 'Server error'));
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content add-service-modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Add New Service</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={handleSubmit} className="service-form">
          {error && (
            <div className="error-alert">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
              </svg>
              {error}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="name">Service Name *</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="e.g., Bridal Makeup, Hair Styling"
              disabled={loading}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Describe your service, include any special details"
              rows="3"
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="price">Price ($) *</label>
            <input
              type="number"
              id="price"
              name="price"
              value={formData.price}
              onChange={handleChange}
              placeholder="0.00"
              step="0.01"
              min="0"
              disabled={loading}
              required
            />
          </div>

          {/* Photo Upload Section */}
          <div className="form-group photo-upload-group">
            <label>Service Photos ({photos.length}/10)</label>
            <div 
              className="photo-drop-zone"
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
            >
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/>
                <line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
              <p className="drop-text">Drag photos here or click to browse</p>
              <p className="drop-subtext">Max 10 photos, 2MB each</p>
              <input 
                type="file" 
                multiple 
                accept="image/*"
                onChange={handlePhotoSelect}
                disabled={loading || photos.length >= 10}
                className="hidden-file-input"
                id="photo-input"
              />
              <label htmlFor="photo-input" className="photo-input-label">
                Choose Photos
              </label>
            </div>

            {/* Photo Previews */}
            {photos.length > 0 && (
              <div className="photo-previews">
                <h4>Uploaded Photos ({photos.length})</h4>
                <div className="photos-grid">
                  {photos.map((photo) => (
                    <div key={photo.id} className="photo-preview-item">
                      <img src={photo.data} alt="Service" />
                      <button 
                        type="button"
                        className="remove-photo-btn"
                        onClick={() => handleRemovePhoto(photo.id)}
                        title="Remove photo"
                      >
                        ✕
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          <div className="modal-footer">
            <button 
              type="button" 
              className="btn-secondary" 
              onClick={onClose}
              disabled={loading}
            >
              Cancel
            </button>
            <button 
              type="submit" 
              className="btn-primary"
              disabled={loading}
            >
              {loading ? 'Creating...' : 'Add Service'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AddServiceModal;

import React, { useState, useEffect } from 'react';
import { apiGet, apiPut } from '../utils/api';
import '../styles/UserProfile.css';

function UserProfile() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [profileImage, setProfileImage] = useState(null);
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const data = await apiGet(`/api/users/${userId}`);
        const userObj = data?.data || data;
        if (userObj) {
          setUser(userObj);
        } else {
          setError('Failed to load profile');
        }
      } catch (err) {
        setError(err.message || 'Connection error. Is the backend running?');
      } finally {
        setLoading(false);
      }
    };

    if (userId) {
      fetchUser();
    }
  }, [userId]);

  // Load existing profile image when user data changes
  useEffect(() => {
    if (user?.profileImage) {
      setProfileImage(user.profileImage);
    }
  }, [user]);

  const handlePhotoUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('image/')) {
      setError('Please select a valid image file');
      return;
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      setError('Image size must be less than 5MB');
      return;
    }

    setUploading(true);
    setError('');
    setMessage('');

    try {
      // Read file as data URL
      const reader = new FileReader();
      reader.onloadend = async () => {
        const base64Image = reader.result;
        
        // Send to backend
        try {
          const response = await apiPut(`/api/users/${userId}/photo`, { 
            profileImage: base64Image 
          });
          
          if (response?.success || response?.data) {
            setProfileImage(base64Image);
            setMessage('Profile photo saved successfully!');
            setTimeout(() => setMessage(''), 3000);
          } else {
            setError('Failed to save photo on server');
          }
        } catch (err) {
          setError('Failed to upload photo: ' + (err.message || 'Server error'));
        } finally {
          setUploading(false);
        }
      };
      reader.readAsDataURL(file);
    } catch (err) {
      setError('Failed to process image');
      setUploading(false);
    }
  };

  if (loading) {
    return (
      <div className="page-section">
        <h2 className="section-title">Profile</h2>
        <p className="section-sub">View and update your personal information</p>
        <div className="empty-state">
          <p>Loading profile...</p>
        </div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="page-section">
        <h2 className="section-title">Profile</h2>
        <div className="empty-state error-state">
          <p>{error || 'Failed to load profile'}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-section">
      <h2 className="section-title">Update Profile</h2>
      <p className="section-sub">Manage your account information and photo</p>

      <div className="profile-container">
        <div className="profile-info-card">
          {/* Profile Photo Section */}
          <div className="profile-photo-section">
            <div className="profile-photo-wrapper">
              {profileImage ? (
                <img src={profileImage} alt="Profile" className="profile-photo" />
              ) : (
                <div className="profile-avatar-large">
                  {user.name?.charAt(0).toUpperCase()}
                </div>
              )}
              <label htmlFor="photo-upload" className="photo-upload-btn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                  <circle cx="12" cy="13" r="4"/>
                </svg>
                {uploading ? 'Uploading...' : 'Change Photo'}
              </label>
              <input 
                id="photo-upload"
                type="file" 
                accept="image/*" 
                onChange={handlePhotoUpload}
                disabled={uploading}
                style={{ display: 'none' }}
              />
            </div>
            {message && <p className="success-text">{message}</p>}
            {error && <p className="error-text">{error}</p>}
          </div>

          {/* Profile Information */}
          <div className="profile-info-section">
            <div className="profile-header-info">
              <h3 className="profile-name">{user.name}</h3>
              <p className="profile-role">
                {user.role === 'ARTIST' ? '🎨 Artist' : '💅 Client'}
              </p>
            </div>

            <div className="profile-details-grid">
              <div className="detail-card">
                <span className="detail-icon">📧</span>
                <div>
                  <p className="detail-label">Email</p>
                  <p className="detail-value">{user.email}</p>
                </div>
              </div>

              <div className="detail-card">
                <span className="detail-icon">👤</span>
                <div>
                  <p className="detail-label">Full Name</p>
                  <p className="detail-value">{user.name}</p>
                </div>
              </div>

              <div className="detail-card">
                <span className="detail-icon">🎭</span>
                <div>
                  <p className="detail-label">Account Type</p>
                  <p className="detail-value">{user.role === 'ARTIST' ? 'Artist Account' : 'Client Account'}</p>
                </div>
              </div>

              <div className="detail-card">
                <span className="detail-icon">🔑</span>
                <div>
                  <p className="detail-label">User ID</p>
                  <p className="detail-value">#{user.id}</p>
                </div>
              </div>

              {user.createdAt && (
                <div className="detail-card">
                  <span className="detail-icon">📅</span>
                  <div>
                    <p className="detail-label">Member Since</p>
                    <p className="detail-value">
                      {new Date(user.createdAt).toLocaleDateString('en-US', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric'
                      })}
                    </p>
                  </div>
                </div>
              )}

              {user.phone && (
                <div className="detail-card">
                  <span className="detail-icon">📱</span>
                  <div>
                    <p className="detail-label">Phone</p>
                    <p className="detail-value">{user.phone}</p>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Additional Info */}
          <div className="profile-section-divider">
            <h4>Account Details</h4>
          </div>

          <div className="account-info-grid">
            <div className="info-row">
              <span className="info-label">Status</span>
              <span className="info-value">
                <span className="status-badge active">Active</span>
              </span>
            </div>

            <div className="info-row">
              <span className="info-label">Email Verified</span>
              <span className="info-value">
                <span className="status-badge verified">✓ Verified</span>
              </span>
            </div>

            {user.bio && (
              <div className="info-row bio-row">
                <span className="info-label">Bio</span>
                <span className="info-value">{user.bio}</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default UserProfile;

import React, { useState, useEffect, useCallback } from 'react';
import { apiGet, apiPut } from '../../shared/api';
import './UserProfile.css';

function UserProfile() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [profileImage, setProfileImage] = useState(null);
  const [coverImage, setCoverImage] = useState(null);
  const [activeTab, setActiveTab] = useState('about');
  const [stats, setStats] = useState({ followers: 0, following: 0, services: 0, appointments: 0 });
  const [editingBio, setEditingBio] = useState(false);
  const [bioText, setBioText] = useState('');
  const [editingDetails, setEditingDetails] = useState(false);
  const [detailForm, setDetailForm] = useState({ name: '', email: '', phone: '', address: '' });
  const userId = localStorage.getItem('userId');
  const userRole = localStorage.getItem('userRole') || 'CLIENT';

  const fetchUser = useCallback(async () => {
    try {
      const data = await apiGet(`/api/users/${userId}`);
      const userObj = data?.data || data;
      if (userObj) {
        setUser(userObj);
        setBioText(userObj.bio || '');
        setDetailForm({ name: userObj.name || '', email: userObj.email || '', phone: userObj.phone || '', address: userObj.address || '' });
      } else {
        setError('Failed to load profile');
      }
    } catch (err) {
      setError(err.message || 'Connection error');
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [svcData, apptData, followData] = await Promise.all([
          apiGet(userRole === 'ARTIST' ? `/api/services?artistId=${userId}` : `/api/services?clientId=${userId}`).catch(() => []),
          apiGet(userRole === 'ARTIST' ? `/api/appointments/artist/${userId}` : `/api/appointments?clientId=${userId}`).catch(() => []),
          apiGet(`/api/users/${userId}/stats`).catch(() => null),
        ]);
        const svcs = Array.isArray(svcData) ? svcData : (svcData?.data || []);
        const appts = Array.isArray(apptData) ? apptData : (apptData?.data || []);
        const followStats = followData?.data || followData || {};
        setStats(prev => ({
          ...prev,
          services: svcs.length,
          appointments: appts.length,
          followers: followStats.followers ?? 0,
          following: followStats.following ?? 0,
        }));
      } catch { /* ignore */ }
    };
    if (userId) { fetchUser(); fetchStats(); }
  }, [userId, userRole, fetchUser]);

  useEffect(() => {
    if (user?.profileImage) setProfileImage(user.profileImage);
    if (user?.coverImage) setCoverImage(user.coverImage);
  }, [user]);

  const uploadImage = async (file, type) => {
    if (!file) return;
    if (!file.type.startsWith('image/')) { setError('Please select a valid image file'); return; }
    if (file.size > 5 * 1024 * 1024) { setError('Image must be less than 5MB'); return; }
    setUploading(true); setError(''); setMessage('');
    const reader = new FileReader();
    reader.onloadend = async () => {
      try {
        const base64 = reader.result;
        if (type === 'profile') {
          const res = await apiPut(`/api/users/${userId}/photo`, { profileImage: base64 });
          if (res?.success || res?.data) { setProfileImage(base64); setMessage('Profile photo updated!'); }
        } else {
          const res = await apiPut(`/api/users/${userId}/cover`, { coverImage: base64 });
          if (res?.success || res?.data) { setCoverImage(base64); setMessage('Cover photo updated!'); }
        }
        setTimeout(() => setMessage(''), 3000);
      } catch (err) {
        setError(err.message || 'Upload failed');
      } finally { setUploading(false); }
    };
    reader.readAsDataURL(file);
  };

  const saveBio = async () => {
    setError(''); setMessage('');
    try {
      const res = await apiPut(`/api/users/${userId}`, { bio: bioText });
      const updated = res?.data || res;
      if (updated) {
        setUser(prev => ({ ...prev, bio: bioText }));
        setMessage('Bio updated!');
        setTimeout(() => setMessage(''), 3000);
      }
    } catch (err) { setError(err.message || 'Failed to save bio'); }
    setEditingBio(false);
  };

  const saveDetails = async () => {
    setError(''); setMessage('');
    try {
      const res = await apiPut(`/api/users/${userId}`, detailForm);
      const updated = res?.data || res;
      if (updated) {
        setUser(prev => ({ ...prev, ...detailForm }));
        localStorage.setItem('userName', detailForm.name);
        setMessage('Profile updated!');
        setTimeout(() => setMessage(''), 3000);
      }
    } catch (err) { setError(err.message || 'Failed to save details'); }
    setEditingDetails(false);
  };

  if (loading) return <div className="fb-profile-root"><div className="fb-loading">Loading profile...</div></div>;
  if (!user) return <div className="fb-profile-root"><div className="fb-loading fb-error">{error || 'Failed to load profile'}</div></div>;

  const initial = user.name?.charAt(0)?.toUpperCase() || '?';
  const memberSince = user.createdAt
    ? new Date(user.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long' })
    : null;

  return (
    <div className="fb-profile-root">
      {/* ── Cover Photo ── */}
      <div className="fb-cover" style={coverImage ? { backgroundImage: `url(${coverImage})`, backgroundSize: 'cover', backgroundPosition: 'center' } : {}}>
        {!coverImage && <div className="fb-cover-gradient" />}
        <label htmlFor="cover-upload" className="fb-cover-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
            <circle cx="12" cy="13" r="4"/>
          </svg>
          Add cover photo
        </label>
        <input id="cover-upload" type="file" accept="image/*" onChange={e => uploadImage(e.target.files?.[0], 'cover')} disabled={uploading} style={{ display: 'none' }} />
      </div>

      {/* ── Profile Header ── */}
      <div className="fb-header">
        <div className="fb-header-row">
          <div className="fb-avatar-wrap">
            {profileImage ? (
              <img src={profileImage} alt="Profile" className="fb-avatar-img" />
            ) : (
              <div className="fb-avatar-placeholder">{initial}</div>
            )}
            <label htmlFor="photo-upload" className="fb-avatar-edit">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                <circle cx="12" cy="13" r="4"/>
              </svg>
            </label>
            <input id="photo-upload" type="file" accept="image/*" onChange={e => uploadImage(e.target.files?.[0], 'profile')} disabled={uploading} style={{ display: 'none' }} />
          </div>

          <div className="fb-header-info">
            <h1 className="fb-name">{user.name}</h1>
            <p className="fb-follower-text">
              <span className="fb-follower-count">{stats.followers}</span> follower{stats.followers !== 1 ? 's' : ''}
              {' · '}
              <span className="fb-follower-count">{stats.following}</span> following
            </p>
          </div>

          <div className="fb-header-actions">
            <label htmlFor="photo-upload" className="fb-action-btn fb-action-primary">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                <circle cx="12" cy="13" r="4"/>
              </svg>
              Edit photo
            </label>
          </div>
        </div>

        {message && <div className="fb-toast fb-toast-ok">{message}</div>}
        {error && <div className="fb-toast fb-toast-err">{error}</div>}
      </div>

      {/* ── Tabs ── */}
      <div className="fb-tabs">
        <button className={`fb-tab${activeTab === 'about' ? ' active' : ''}`} onClick={() => setActiveTab('about')}>About</button>
        <button className={`fb-tab${activeTab === 'details' ? ' active' : ''}`} onClick={() => setActiveTab('details')}>Details</button>
        <button className={`fb-tab${activeTab === 'reviews' ? ' active' : ''}`} onClick={() => setActiveTab('reviews')}>Reviews</button>
      </div>

      {/* ── Tab Content ── */}
      <div className="fb-body">
        {activeTab === 'about' && (
          <div className="fb-two-col">
            <div className="fb-col-left">
              {/* Intro Card - Editable Bio */}
              <div className="fb-card">
                <div className="fb-card-header">
                  <h3 className="fb-card-title">Intro</h3>
                  {!editingBio && (
                    <button className="fb-edit-btn" onClick={() => setEditingBio(true)}>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                      Edit
                    </button>
                  )}
                </div>

                {editingBio ? (
                  <div className="fb-edit-section">
                    <textarea
                      className="fb-bio-input"
                      value={bioText}
                      onChange={e => setBioText(e.target.value)}
                      placeholder="Write something about yourself..."
                      rows={3}
                      maxLength={300}
                    />
                    <div className="fb-edit-actions">
                      <button className="fb-save-btn" onClick={saveBio}>Save</button>
                      <button className="fb-cancel-btn" onClick={() => { setEditingBio(false); setBioText(user.bio || ''); }}>Cancel</button>
                    </div>
                  </div>
                ) : (
                  <>
                    {user.bio ? (
                      <p className="fb-bio">{user.bio}</p>
                    ) : (
                      <button className="fb-add-bio-btn" onClick={() => setEditingBio(true)}>+ Add bio</button>
                    )}
                  </>
                )}

                <div className="fb-info-item">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <span>{userRole === 'ARTIST' ? 'Beauty Artist' : 'Client'} on GlamSched</span>
                </div>
                <div className="fb-info-item">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M2 7l10 7 10-7"/></svg>
                  <span>{user.email}</span>
                </div>
                {user.phone && (
                  <div className="fb-info-item">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.79 19.79 0 0 1 2.12 4.18 2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.13.81.36 1.61.68 2.37a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.71-1.25a2 2 0 0 1 2.11-.45c.76.32 1.56.55 2.37.68A2 2 0 0 1 22 16.92z"/></svg>
                    <span>{user.phone}</span>
                  </div>
                )}
                {user.address && (
                  <div className="fb-info-item">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                    <span>{user.address}</span>
                  </div>
                )}
                {memberSince && (
                  <div className="fb-info-item">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                    <span>Joined {memberSince}</span>
                  </div>
                )}
              </div>
            </div>

            <div className="fb-col-right">
              <div className="fb-card">
                <h3 className="fb-card-title">Overview</h3>
                <div className="fb-stats-grid">
                  <div className="fb-stat-item">
                    <span className="fb-stat-num">{stats.services}</span>
                    <span className="fb-stat-label">{userRole === 'ARTIST' ? 'Services' : 'Available'}</span>
                  </div>
                  <div className="fb-stat-item">
                    <span className="fb-stat-num">{stats.appointments}</span>
                    <span className="fb-stat-label">Appointments</span>
                  </div>
                  <div className="fb-stat-item">
                    <span className="fb-stat-num">{stats.followers}</span>
                    <span className="fb-stat-label">Followers</span>
                  </div>
                  <div className="fb-stat-item">
                    <span className="fb-stat-num">{stats.following}</span>
                    <span className="fb-stat-label">Following</span>
                  </div>
                </div>
              </div>
              <div className="fb-card">
                <h3 className="fb-card-title">Account Status</h3>
                <div className="fb-status-row">
                  <span className="fb-badge fb-badge-green">Active</span>
                  <span className="fb-badge fb-badge-blue">Verified</span>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'details' && (
          <div className="fb-details">
            <div className="fb-card">
              <div className="fb-card-header">
                <h3 className="fb-card-title">Personal Information</h3>
                {!editingDetails && (
                  <button className="fb-edit-btn" onClick={() => { setEditingDetails(true); setDetailForm({ name: user.name || '', email: user.email || '', phone: user.phone || '', address: user.address || '' }); }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                    Edit
                  </button>
                )}
              </div>

              {editingDetails ? (
                <div className="fb-edit-section">
                  <div className="fb-form-group">
                    <label className="fb-form-label" htmlFor="edit-name">Full Name</label>
                    <input id="edit-name" className="fb-form-input" value={detailForm.name} onChange={e => setDetailForm(p => ({ ...p, name: e.target.value }))} />
                  </div>
                  <div className="fb-form-group">
                    <label className="fb-form-label" htmlFor="edit-email">Email</label>
                    <input id="edit-email" className="fb-form-input" type="email" value={detailForm.email} onChange={e => setDetailForm(p => ({ ...p, email: e.target.value }))} />
                  </div>
                  <div className="fb-form-group">
                    <label className="fb-form-label" htmlFor="edit-phone">Phone</label>
                    <input id="edit-phone" className="fb-form-input" value={detailForm.phone} onChange={e => setDetailForm(p => ({ ...p, phone: e.target.value }))} placeholder="Enter phone number" />
                  </div>
                  <div className="fb-form-group">
                    <label className="fb-form-label" htmlFor="edit-address">Address</label>
                    <input id="edit-address" className="fb-form-input" value={detailForm.address} onChange={e => setDetailForm(p => ({ ...p, address: e.target.value }))} placeholder="Enter address" />
                  </div>
                  <div className="fb-edit-actions">
                    <button className="fb-save-btn" onClick={saveDetails}>Save Changes</button>
                    <button className="fb-cancel-btn" onClick={() => setEditingDetails(false)}>Cancel</button>
                  </div>
                </div>
              ) : (
                <div className="fb-detail-grid">
                  <div className="fb-detail-row"><span className="fb-detail-label">Full Name</span><span className="fb-detail-value">{user.name}</span></div>
                  <div className="fb-detail-row"><span className="fb-detail-label">Email</span><span className="fb-detail-value">{user.email}</span></div>
                  <div className="fb-detail-row"><span className="fb-detail-label">Account Type</span><span className="fb-detail-value">{userRole === 'ARTIST' ? 'Artist' : 'Client'}</span></div>
                  <div className="fb-detail-row"><span className="fb-detail-label">Phone</span><span className="fb-detail-value">{user.phone || '—'}</span></div>
                  <div className="fb-detail-row"><span className="fb-detail-label">Address</span><span className="fb-detail-value">{user.address || '—'}</span></div>
                  <div className="fb-detail-row"><span className="fb-detail-label">User ID</span><span className="fb-detail-value">#{user.id}</span></div>
                  {memberSince && <div className="fb-detail-row"><span className="fb-detail-label">Member Since</span><span className="fb-detail-value">{memberSince}</span></div>}
                </div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'reviews' && (
          <ProfileReviews userId={userId} userRole={userRole} />
        )}
      </div>
    </div>
  );
}

function ProfileReviews({ userId, userRole }) {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [avgRating, setAvgRating] = useState(0);

  useEffect(() => {
    const load = async () => {
      try {
        const data = await apiGet(`/api/reviews/artist/${userId}`);
        const list = Array.isArray(data) ? data : (data?.data || []);
        setReviews(list);
        if (list.length > 0) {
          const avg = list.reduce((sum, r) => sum + (r.rating || 0), 0) / list.length;
          setAvgRating(avg);
        }
      } catch { /* ignore */ }
      finally { setLoading(false); }
    };
    load();
  }, [userId, userRole]);

  const renderStars = (rating) => {
    return Array.from({ length: 5 }, (_, i) => (
      <span key={i} className={`fb-star${i < rating ? ' filled' : ''}`}>★</span>
    ));
  };

  if (loading) return <div className="fb-card"><p style={{ textAlign: 'center', padding: '40px', color: '#999' }}>Loading reviews...</p></div>;

  return (
    <div className="fb-reviews-section">
      {/* Summary Card */}
      <div className="fb-card fb-review-summary">
        <div className="fb-review-avg">
          <span className="fb-review-avg-num">{avgRating > 0 ? avgRating.toFixed(1) : '—'}</span>
          <div className="fb-review-avg-stars">{renderStars(Math.round(avgRating))}</div>
          <span className="fb-review-avg-count">{reviews.length} review{reviews.length !== 1 ? 's' : ''}</span>
        </div>
      </div>

      {reviews.length === 0 ? (
        <div className="fb-card">
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" strokeWidth="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            <p style={{ color: '#999', marginTop: '12px', fontSize: '14px' }}>No reviews yet</p>
          </div>
        </div>
      ) : (
        <div className="fb-reviews-list">
          {reviews.map(review => (
            <div key={review.id} className="fb-card fb-review-card">
              <div className="fb-review-header">
                <div className="fb-review-avatar">
                  {(review.clientName || 'U').charAt(0).toUpperCase()}
                </div>
                <div className="fb-review-meta">
                  <span className="fb-review-name">{review.clientName || 'Anonymous'}</span>
                  <span className="fb-review-date">{review.createdAt || ''}</span>
                </div>
                <div className="fb-review-stars">{renderStars(review.rating)}</div>
              </div>
              {review.comment && <p className="fb-review-comment">{review.comment}</p>}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default UserProfile;

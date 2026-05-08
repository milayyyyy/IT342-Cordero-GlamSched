import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiGet, apiPost } from '../../shared/api';
import './ArtistProfile.css';

export default function ArtistProfile() {
  const { artistId } = useParams();
  const navigate = useNavigate();
  const clientId = localStorage.getItem('userId');
  const isOwner = String(clientId) === String(artistId);

  const [artist, setArtist] = useState(null);
  const [services, setServices] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [avgRating, setAvgRating] = useState(0);
  const [loading, setLoading] = useState(true);
  const [photoIndexes, setPhotoIndexes] = useState({});
  const [activeTab, setActiveTab] = useState('about');
  const [selectedPhoto, setSelectedPhoto] = useState(null);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [reviewMsg, setReviewMsg] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [profileRes, servicesRes, reviewsRes] = await Promise.all([
        apiGet(`/api/users/${artistId}/profile?clientId=${clientId}`),
        apiGet(`/api/services/artist/${artistId}?clientId=${clientId}`),
        apiGet(`/api/reviews/artist/${artistId}`).catch(() => []),
      ]);
      setArtist(profileRes?.data || profileRes);
      const list = servicesRes?.data || servicesRes || [];
      setServices(Array.isArray(list) ? list : []);
      const rvList = Array.isArray(reviewsRes) ? reviewsRes : (reviewsRes?.data || []);
      setReviews(rvList);
      if (rvList.length > 0) {
        const avg = rvList.reduce((sum, r) => sum + (r.rating || 0), 0) / rvList.length;
        setAvgRating(avg);
      }
    } catch (e) {
      console.error('Failed to load artist profile', e);
    } finally {
      setLoading(false);
    }
  }, [artistId, clientId]);

  useEffect(() => { loadData(); }, [loadData]);

  const handleFollow = async () => {
    try {
      const res = await apiPost(`/api/users/${artistId}/follow?clientId=${clientId}`);
      const result = res?.data || res;
      setArtist(prev => ({
        ...prev,
        followedByMe: result.followedByMe,
        followerCount: result.followerCount ?? prev.followerCount,
      }));
    } catch (e) {
      console.error('Follow failed', e);
    }
  };

  const handleReact = async (e, service) => {
    e.stopPropagation();
    try {
      const res = await apiPost(`/api/services/${service.id}/react?clientId=${clientId}`);
      const result = res?.data || res;
      setServices(prev => prev.map(s =>
        s.id === service.id ? { ...s, likedByMe: result.likedByMe, reactionCount: result.reactionCount } : s
      ));
    } catch (e) {
      console.error('Reaction failed', e);
    }
  };

  const photoNext = (e, service) => {
    e.stopPropagation();
    const photos = service.photos || [];
    if (photos.length < 2) return;
    setPhotoIndexes(prev => ({ ...prev, [service.id]: ((prev[service.id] || 0) + 1) % photos.length }));
  };

  const photoPrev = (e, service) => {
    e.stopPropagation();
    const photos = service.photos || [];
    if (photos.length < 2) return;
    setPhotoIndexes(prev => ({ ...prev, [service.id]: ((prev[service.id] || 0) - 1 + photos.length) % photos.length }));
  };

  const handleBook = (service) => {
    localStorage.setItem('selectedService', JSON.stringify(service));
    navigate('/dashboard/booknow');
  };

  const submitReview = async () => {
    if (!reviewComment.trim()) return;
    setSubmittingReview(true);
    setReviewMsg('');
    try {
      const clientName = localStorage.getItem('userName') || 'Client';
      const res = await apiPost('/api/reviews', {
        artistId: Number(artistId),
        clientId: Number(clientId),
        clientName,
        rating: reviewRating,
        comment: reviewComment.trim(),
      });
      const saved = res?.data || res;
      setReviews(prev => [saved, ...prev]);
      const newList = [saved, ...reviews];
      const avg = newList.reduce((sum, r) => sum + (r.rating || 0), 0) / newList.length;
      setAvgRating(avg);
      setReviewComment('');
      setReviewRating(5);
      setReviewMsg('Review submitted!');
      setTimeout(() => setReviewMsg(''), 3000);
    } catch (e) {
      setReviewMsg(e.message || 'Failed to submit review.');
    } finally {
      setSubmittingReview(false);
    }
  };

  const renderStars = (rating) =>
    Array.from({ length: 5 }, (_, i) => (
      <span key={i} className={`fb-star${i < rating ? ' filled' : ''}`}>★</span>
    ));

  if (loading) return <div className="ap-loading">Loading profile…</div>;

  const initial = artist?.name?.charAt(0)?.toUpperCase() || '?';

  return (
    <div className="ap-root">
      {/* Cover */}
      <div
        className="ap-cover"
        style={artist?.coverImage ? { backgroundImage: `url(${artist.coverImage})`, backgroundSize: 'cover', backgroundPosition: 'center' } : {}}
      >
        {!artist?.coverImage && <div className="ap-cover-gradient" />}
      </div>

      {/* Profile Header */}
      <div className="fb-header">
        <div className="fb-header-row">
          <div className="fb-avatar-wrap">
            {artist?.profileImage ? (
              <img src={artist.profileImage} alt={artist.name} className="fb-avatar-img" />
            ) : (
              <div className="fb-avatar-placeholder">{initial}</div>
            )}
          </div>
          <div className="fb-header-info">
            <h1 className="fb-name">{artist?.name || 'Artist'}</h1>
            <p className="fb-follower-text">
              <span className="fb-follower-count">{artist?.followerCount ?? 0}</span> follower{(artist?.followerCount ?? 0) !== 1 ? 's' : ''}
              {' · '}
              <span className="fb-follower-count">{services.length}</span> service{services.length !== 1 ? 's' : ''}
            </p>
          </div>
          <div className="ap-header-actions">
            <button
              className={`ap-follow-btn-inline${artist?.followedByMe ? ' following' : ''}`}
              onClick={handleFollow}
            >
              {artist?.followedByMe ? '✓ Following' : '+ Follow'}
            </button>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="fb-tabs">
        <button className={`fb-tab${activeTab === 'about' ? ' active' : ''}`} onClick={() => setActiveTab('about')}>About</button>
        <button className={`fb-tab${activeTab === 'portfolio' ? ' active' : ''}`} onClick={() => setActiveTab('portfolio')}>Portfolio</button>
        <button className={`fb-tab${activeTab === 'reviews' ? ' active' : ''}`} onClick={() => setActiveTab('reviews')}>Reviews</button>
      </div>

      {/* Tab Content */}
      <div className="fb-body">

        {/* ── ABOUT TAB ── */}
        {activeTab === 'about' && (
          <div className="fb-two-col">
            {/* Left: Intro + Account Status */}
            <div className="fb-col-left">
              <div className="fb-card">
                <h3 className="fb-card-title">Intro</h3>
                {artist?.bio ? (
                  <p className="fb-bio">{artist.bio}</p>
                ) : (
                  <p style={{ color: '#aaa', fontSize: '14px' }}>No bio added yet.</p>
                )}
                <div className="fb-info-item">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <span>Beauty Artist on GlamSched</span>
                </div>
                {artist?.email && (
                  <div className="fb-info-item">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M2 7l10 7 10-7"/></svg>
                    <span>{artist.email}</span>
                  </div>
                )}
                {artist?.phone && (
                  <div className="fb-info-item">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.79 19.79 0 0 1 2.12 4.18 2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.13.81.36 1.61.68 2.37a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.71-1.25a2 2 0 0 1 2.11-.45c.76.32 1.56.55 2.37.68A2 2 0 0 1 22 16.92z"/></svg>
                    <span>{artist.phone}</span>
                  </div>
                )}
                {artist?.address && (
                  <div className="fb-info-item">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                    <span>{artist.address}</span>
                  </div>
                )}
              </div>

              {/* Account Status below Intro */}
              {isOwner && (
                <div className="fb-card">
                  <h3 className="fb-card-title">Account Status</h3>
                  <div className="fb-status-row">
                    <span className="fb-badge fb-badge-green">Active</span>
                    <span className="fb-badge fb-badge-blue">Verified</span>
                  </div>
                </div>
              )}
            </div>

            {/* Right: Posts feed (full cards) or Overview for owner */}
            <div className="fb-col-right">
              {isOwner ? (
                <div className="fb-card">
                  <h3 className="fb-card-title">Overview</h3>
                  <div className="fb-stats-grid">
                    <div className="fb-stat-item">
                      <span className="fb-stat-num">{services.length}</span>
                      <span className="fb-stat-label">Services</span>
                    </div>
                    <div className="fb-stat-item">
                      <span className="fb-stat-num">{artist?.followerCount ?? 0}</span>
                      <span className="fb-stat-label">Followers</span>
                    </div>
                    <div className="fb-stat-item">
                      <span className="fb-stat-num">{reviews.length}</span>
                      <span className="fb-stat-label">Reviews</span>
                    </div>
                    <div className="fb-stat-item">
                      <span className="fb-stat-num">{avgRating > 0 ? avgRating.toFixed(1) : '—'}</span>
                      <span className="fb-stat-label">Avg Rating</span>
                    </div>
                  </div>
                </div>
              ) : (
                <>
                  {services.length === 0 && (
                    <div className="ap-empty">No services posted yet.</div>
                  )}
                  {services.map(service => (
                    <div key={service.id} className="ap-post-card">
                      <div className="ap-post-header">
                        {artist?.profileImage ? (
                          <img src={artist.profileImage} alt={artist.name} className="ap-post-avatar ap-post-avatar-img" />
                        ) : (
                          <div className="ap-post-avatar">{initial}</div>
                        )}
                        <div className="ap-post-meta">
                          <span className="ap-post-name">{artist?.name || 'Artist'}</span>
                          <span className="ap-post-sub">Beauty Artist · GlamSched</span>
                        </div>
                        {artist?.followedByMe && (
                          <span className="ap-following-badge">✓ Following</span>
                        )}
                        {service.price != null && (
                          <span className="ap-post-price">₱{service.price.toFixed(2)}</span>
                        )}
                      </div>

                      <div className="ap-post-body">
                        <p className="ap-post-title">{service.name}</p>
                        {service.description && <p className="ap-post-desc">{service.description}</p>}
                      </div>

                      {service.photos && service.photos.length > 0 && (
                        <div className="ap-photo-wrap">
                          <img
                            src={service.photos[photoIndexes[service.id] || 0]}
                            alt={service.name}
                            className="ap-photo"
                          />
                          {service.photos.length > 1 && (
                            <>
                              <button className="ap-carousel-btn ap-carousel-prev" onClick={e => photoPrev(e, service)}>&#8249;</button>
                              <button className="ap-carousel-btn ap-carousel-next" onClick={e => photoNext(e, service)}>&#8250;</button>
                              <div className="ap-carousel-dots">
                                {service.photos.map((_, i) => (
                                  <span
                                    key={i}
                                    className={`ap-carousel-dot${(photoIndexes[service.id] || 0) === i ? ' active' : ''}`}
                                    onClick={e => { e.stopPropagation(); setPhotoIndexes(prev => ({ ...prev, [service.id]: i })); }}
                                  />
                                ))}
                              </div>
                            </>
                          )}
                        </div>
                      )}

                      {service.reactionCount > 0 && (
                        <div className="ap-reaction-row">
                          <span>❤️</span>
                          <span>{service.reactionCount}</span>
                        </div>
                      )}

                      <div className="ap-post-actions">
                        <button
                          className={`ap-action-btn${service.likedByMe ? ' liked' : ''}`}
                          onClick={e => handleReact(e, service)}
                        >
                          {service.likedByMe ? '❤️' : '🤍'} Love
                        </button>
                        <button className="ap-action-btn ap-book-btn" onClick={() => handleBook(service)}>
                          📅 Book Now
                        </button>
                      </div>
                    </div>
                  ))}
                </>
              )}
            </div>
          </div>
        )}

        {/* ── PORTFOLIO TAB ── */}
        {activeTab === 'portfolio' && (() => {
          const allPhotos = services.flatMap(s =>
            (s.photos || []).map(photo => ({ photo, serviceName: s.name, price: s.price, description: s.description }))
          );
          return (
            <div className="ap-portfolio">
              {allPhotos.length === 0 ? (
                <div className="ap-empty">
                  <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" strokeWidth="1.5">
                    <rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/>
                    <polyline points="21 15 16 10 5 21"/>
                  </svg>
                  <p>No portfolio photos yet.</p>
                </div>
              ) : (
                <div className="pf-grid">
                  {allPhotos.map((item, i) => (
                    <div key={i} className="pf-item" onClick={() => setSelectedPhoto(item)}>
                      <img src={item.photo} alt={item.serviceName} className="pf-img" />
                      <div className="pf-overlay">
                        <span className="pf-overlay-name">{item.serviceName}</span>
                        {item.price != null && <span className="pf-overlay-price">₱{item.price.toFixed(2)}</span>}
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {selectedPhoto && (
                <div className="pf-lightbox" onClick={() => setSelectedPhoto(null)}>
                  <div className="pf-lightbox-content" onClick={e => e.stopPropagation()}>
                    <button className="pf-lightbox-close" onClick={() => setSelectedPhoto(null)}>✕</button>
                    <img src={selectedPhoto.photo} alt={selectedPhoto.serviceName} className="pf-lightbox-img" />
                    <div className="pf-lightbox-info">
                      <h3>{selectedPhoto.serviceName}</h3>
                      {selectedPhoto.description && <p className="pf-lightbox-desc">{selectedPhoto.description}</p>}
                      {selectedPhoto.price != null && <span className="pf-lightbox-price">₱{selectedPhoto.price.toFixed(2)}</span>}
                      <p className="pf-lightbox-artist">by {artist?.name}</p>
                    </div>
                  </div>
                </div>
              )}
            </div>
          );
        })()}

        {/* ── REVIEWS TAB ── */}
        {activeTab === 'reviews' && (
          <div className="fb-reviews-section">

            {/* Write a Review (clients only) */}
            {!isOwner && (
              <div className="fb-card ap-review-form">
                <h3 className="fb-card-title">Write a Review</h3>
                <div className="ap-star-picker">
                  {[1,2,3,4,5].map(n => (
                    <button
                      key={n}
                      className={`ap-star-btn${n <= reviewRating ? ' selected' : ''}`}
                      onClick={() => setReviewRating(n)}
                    >★</button>
                  ))}
                  <span className="ap-star-label">{reviewRating} / 5</span>
                </div>
                <textarea
                  className="fb-bio-input"
                  placeholder="Share your experience with this artist..."
                  value={reviewComment}
                  onChange={e => setReviewComment(e.target.value)}
                  rows={3}
                  maxLength={500}
                />
                <div className="ap-review-form-footer">
                  {reviewMsg && (
                    <span className={`ap-review-msg${reviewMsg.includes('Failed') ? ' err' : ''}`}>{reviewMsg}</span>
                  )}
                  <button
                    className="ap-follow-btn-inline"
                    onClick={submitReview}
                    disabled={submittingReview || !reviewComment.trim()}
                  >
                    {submittingReview ? 'Submitting…' : 'Submit Review'}
                  </button>
                </div>
              </div>
            )}

            {/* Rating summary */}
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
        )}

      </div>
    </div>
  );
}

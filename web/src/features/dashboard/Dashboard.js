import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import BookAppointment from '../booking/BookAppointment';
import BookingFlow from '../booking/BookingFlow';
import ArtistAppointments from '../booking/ArtistAppointments';
import UserProfile from '../user/UserProfile';
import ArtistProfile from '../user/ArtistProfile';
import MyAppointments from '../booking/MyAppointments';
import PaymentHistory from '../payment/PaymentHistory';
import PaymentPage from '../payment/PaymentPage';
import AddServiceModal from '../services/AddServiceModal';
import { apiGet, apiPost, apiPut, apiDelete } from '../../shared/api';
import './Dashboard.css';

// Artist Services Component with Search Filter + Detail Modal
function ArtistServices() {
  const [services, setServices] = useState([]);
  const [filteredServices, setFilteredServices] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showAddServiceModal, setShowAddServiceModal] = useState(false);
  const [selectedService, setSelectedService] = useState(null);
  const [activePhoto, setActivePhoto] = useState(0);
  const artistId = localStorage.getItem('userId');

  const fetchServices = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await apiGet(`/api/services?artistId=${artistId}`);
      const list = Array.isArray(data) ? data : (data.data || []);
      setServices(list);
      setFilteredServices(list);
    } catch (err) {
      setError(err.message || 'Failed to fetch services');
      setServices([]);
      setFilteredServices([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (artistId) fetchServices();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [artistId]);

  const handleDeleteService = (e, service) => {
    e.stopPropagation();
    // eslint-disable-next-line no-undef
    if (globalThis.confirm('Delete service: ' + service.name + '?')) {
      setServices(services.filter(s => s.id !== service.id));
      setFilteredServices(filteredServices.filter(s => s.id !== service.id));
    }
  };

  const handleServiceAdded = () => { fetchServices(); };

  const handleSearch = (e) => {
    const term = e.target.value.toLowerCase();
    setSearchTerm(term);
    setFilteredServices(services.filter(service =>
      service.name?.toLowerCase().includes(term) ||
      service.description?.toLowerCase().includes(term)
    ));
  };

  return (
    <div className="page-section">
      <div className="services-header">
        <div>
          <h2 className="section-title">My Services</h2>
          <p className="section-sub">Manage and view your beauty services</p>
        </div>
        <button className="btn-add-service" onClick={() => setShowAddServiceModal(true)}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Add Service
        </button>
      </div>

      <div className="service-search-container">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="search-icon">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
        </svg>
        <input type="text" placeholder="Search services by name..." value={searchTerm} onChange={handleSearch} className="service-search-input" />
      </div>

      {loading && <div className="empty-state"><p>Loading services...</p></div>}

      {error && (
        <div className="empty-state error-state">
          <p>Error: {error}</p>
          <button className="btn-secondary" onClick={fetchServices} style={{marginTop:'16px'}}>Retry</button>
        </div>
      )}

      {!loading && filteredServices.length === 0 && (
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/>
          </svg>
          <p>{searchTerm ? 'No services match your search' : 'No services yet'}</p>
        </div>
      )}

      {!loading && filteredServices.length > 0 && (
        <div className="as-grid">
          {filteredServices.map(service => (
            <div key={service.id} className="as-card" onClick={() => { setSelectedService(service); setActivePhoto(0); }}>
              {service.photos && service.photos.length > 0 ? (
                <div className="as-card-photo">
                  <img src={service.photos[0]} alt={service.name} className="as-card-img" />
                  {service.photos.length > 1 && (
                    <span className="as-photo-count">+{service.photos.length - 1}</span>
                  )}
                </div>
              ) : (
                <div className="as-card-photo as-card-no-photo">
                  <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                    <path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/>
                  </svg>
                </div>
              )}
              <div className="as-card-body">
                <h3 className="as-card-name">{service.name}</h3>
                {service.description && <p className="as-card-desc">{service.description}</p>}
                <div className="as-card-price">₱{service.price?.toFixed(2) || '0.00'}</div>
              </div>
              <div className="as-card-actions">
                <button className="as-action-btn" onClick={(e) => { e.stopPropagation(); alert('Edit: ' + service.name + '\nComing soon!'); }}>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15H9v-3L18.5 2.5z"/>
                  </svg>
                  Edit
                </button>
                <button className="as-action-btn as-action-danger" onClick={(e) => handleDeleteService(e, service)}>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                  </svg>
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Detail Modal */}
      {selectedService && (
        <div className="svc-modal-overlay" onClick={() => setSelectedService(null)}>
          <div className="svc-modal" onClick={e => e.stopPropagation()}>
            <button className="svc-modal-close" onClick={() => setSelectedService(null)}>✕</button>
            {selectedService.photos && selectedService.photos.length > 0 && (
              <div className="svc-modal-photo">
                <img src={selectedService.photos[activePhoto]} alt={selectedService.name} className="svc-modal-photo-img" />
                {selectedService.photos.length > 1 && (
                  <div className="svc-modal-photo-thumbs">
                    {selectedService.photos.map((p, i) => (
                      <img key={i} src={p} alt={`thumbnail ${i + 1}`} className={`svc-modal-thumb${activePhoto === i ? ' active' : ''}`} onClick={() => setActivePhoto(i)} />
                    ))}
                  </div>
                )}
              </div>
            )}
            <h2 className="svc-modal-title">{selectedService.name}</h2>
            {selectedService.description && <p className="svc-modal-description">{selectedService.description}</p>}
            <div className="svc-modal-details">
              <div className="svc-modal-detail-item">
                <span className="svc-modal-detail-label">Price</span>
                <span className="svc-modal-detail-value svc-modal-price">₱{selectedService.price?.toFixed(2) || '0.00'}</span>
              </div>
              {selectedService.category && (
                <div className="svc-modal-detail-item">
                  <span className="svc-modal-detail-label">Category</span>
                  <span className="svc-modal-detail-value">{selectedService.category}</span>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      <AddServiceModal
        isOpen={showAddServiceModal}
        onClose={() => setShowAddServiceModal(false)}
        onServiceAdded={handleServiceAdded}
        artistId={artistId}
      />
    </div>
  );
}

// CLIENT Navigation
const clientNavItems = [
  {
    key: 'overview',
    label: 'Beauty Feed',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
        <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
      </svg>
    ),
  },
  {
    key: 'myappointments',
    label: 'My Appointments',
    group: 'BOOKINGS',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/>
        <line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
      </svg>
    ),
  },
  {
    key: 'favorites',
    label: 'Favorites',
    group: 'BOOKINGS',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
      </svg>
    ),
  },
  {
    key: 'paymenthistory',
    label: 'Payment History',
    group: 'FINANCE',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/>
      </svg>
    ),
  },
  {
    key: 'profile',
    label: 'Profile',
    group: 'ACCOUNT',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
      </svg>
    ),
  },
  {
    key: 'settings',
    label: 'Settings',
    group: 'ACCOUNT',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="3"/>
        <path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83"/>
      </svg>
    ),
  },
];

// CLIENT Dashboard - Welcome + Stats + Browse
// eslint-disable-next-line react/prop-types
function ClientDashboard({ onNavigate }) {
  const navigate = useNavigate();
  const [services, setServices] = useState([]);
  const FEED_CATEGORIES = ['All', 'Hair', 'Makeup', 'Nails', 'Skincare', 'Lashes', 'Brows', 'Waxing', 'Massage', 'Other'];
  const [filteredServices, setFilteredServices] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [activeCategory, setActiveCategory] = useState('All');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [stats, setStats] = useState({ upcoming: 0, completed: 0, totalSpent: 0 });
  const [selectedService, setSelectedService] = useState(null);
  const [activePhoto, setActivePhoto] = useState(0);
  const [photoIndexes, setPhotoIndexes] = useState({});
  const favoritesRef = useRef(JSON.parse(localStorage.getItem('favoriteServices') || '[]'));
  const clientId = localStorage.getItem('userId');
  const userName = localStorage.getItem('userName') || 'Client';

  const photoNext = useCallback((e, service) => {
    e.stopPropagation();
    const photos = service.photos || [];
    if (photos.length < 2) return;
    setPhotoIndexes(prev => ({
      ...prev,
      [service.id]: ((prev[service.id] || 0) + 1) % photos.length
    }));
  }, []);

  const photoPrev = useCallback((e, service) => {
    e.stopPropagation();
    const photos = service.photos || [];
    if (photos.length < 2) return;
    setPhotoIndexes(prev => ({
      ...prev,
      [service.id]: ((prev[service.id] || 0) - 1 + photos.length) % photos.length
    }));
  }, []);

  const fetchAll = async () => {
    try {
      setLoading(true);
      setError(null);
      const [svcData, apptData] = await Promise.all([
        apiGet(`/api/services?clientId=${clientId}`),
        apiGet(`/api/appointments?clientId=${clientId}`).catch(() => []),
      ]);
      const serviceList = Array.isArray(svcData) ? svcData : (svcData.data || []);
      setServices(serviceList);
      setFilteredServices(serviceList);
      const appts = Array.isArray(apptData) ? apptData : (apptData.data || []);

      const completedAppts = appts.filter(a => a.status?.toUpperCase() === 'COMPLETED');

      // Build price map from all services
      const servicePriceMap = {};
      try {
        const svcAll = await apiGet('/api/services');
        const allSvcs = Array.isArray(svcAll) ? svcAll : (svcAll?.data || []);
        allSvcs.forEach(s => { if (s.id != null && s.price != null) servicePriceMap[s.id] = Number.parseFloat(s.price) || 0; });
      } catch { /* ignore */ }

      const totalSpent = completedAppts.reduce((sum, a) => sum + (servicePriceMap[a.serviceId] || 0), 0);

      setStats({
        upcoming: appts.filter(a => ['CONFIRMED', 'PENDING'].includes(a.status?.toUpperCase())).length,
        completed: completedAppts.length,
        totalSpent,
      });
    } catch (err) {
      setError(err.message || 'Failed to load services');
      setServices([]);
      setFilteredServices([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const applyFilters = (term, cat, list) => {
    return list.filter(s => {
      const matchesSearch = !term ||
        s.name?.toLowerCase().includes(term) ||
        s.description?.toLowerCase().includes(term) ||
        s.artistName?.toLowerCase().includes(term);
      const matchesCat = cat === 'All' || s.category?.toLowerCase() === cat.toLowerCase();
      return matchesSearch && matchesCat;
    });
  };

  const handleSearch = (e) => {
    const term = e.target.value.toLowerCase();
    setSearchTerm(term);
    setFilteredServices(applyFilters(term, activeCategory, services));
  };

  const handleCategoryFilter = (cat) => {
    setActiveCategory(cat);
    setFilteredServices(applyFilters(searchTerm, cat, services));
  };

  const handleBookService = (service) => {
    localStorage.setItem('selectedService', JSON.stringify(service));
    navigate('/dashboard/booknow');
  };

  const handleFollow = async (e, service) => {
    e.stopPropagation();
    try {
      const res = await apiPost(`/api/users/${service.artistId}/follow?clientId=${clientId}`);
      const result = res?.data || res;
      const update = (list) => list.map(s =>
        s.artistId === service.artistId
          ? { ...s, followedByMe: result.followedByMe }
          : s
      );
      setServices(prev => update(prev));
      setFilteredServices(prev => update(prev));
    } catch (err) {
      console.error('Follow failed', err);
    }
  };

  const handleReact = async (e, service) => {    e.stopPropagation();
    try {
      const res = await apiPost(`/api/services/${service.id}/react?clientId=${clientId}`);
      const result = res?.data || res;
      const update = (list) => list.map(s =>
        s.id === service.id
          ? { ...s, likedByMe: result.likedByMe, reactionCount: result.reactionCount }
          : s
      );
      setServices(prev => update(prev));
      setFilteredServices(prev => update(prev));
      if (selectedService?.id === service.id) {
        setSelectedService(prev => ({ ...prev, likedByMe: result.likedByMe, reactionCount: result.reactionCount }));
      }
      // Sync favorites with love reaction
      const updatedFavorites = result.likedByMe
        ? favoritesRef.current.some(f => f.id === service.id)
          ? favoritesRef.current
          : [...favoritesRef.current, { ...service, likedByMe: true }]
        : favoritesRef.current.filter(f => f.id !== service.id);
      favoritesRef.current = updatedFavorites;
      localStorage.setItem('favoriteServices', JSON.stringify(updatedFavorites));
    } catch (err) {
      console.error('Reaction failed', err);
    }
  };

  return (
    <>
      {/* Welcome Banner */}
      <div className="client-welcome-banner">
        <div className="client-welcome-content">
          <div className="client-welcome-text">
            <h2>Hello, <span className="welcome-name">{userName}</span> 👋</h2>
            <p>Ready for your next glam session? Browse and book below.</p>
          </div>
        </div>
      </div>

      {/* Quick Stats */}
      <div className="client-stats-row">
        <button className="cstat-card" onClick={() => onNavigate?.('myappointments')}>
          <div className="cstat-icon-wrap cstat-gold">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
          </div>
          <div>
            <div className="cstat-number">{stats.upcoming}</div>
            <div className="cstat-label">Upcoming</div>
          </div>
        </button>
        <button className="cstat-card" onClick={() => onNavigate?.('myappointments')}>
          <div className="cstat-icon-wrap cstat-green">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </div>
          <div>
            <div className="cstat-number">{stats.completed}</div>
            <div className="cstat-label">Completed</div>
          </div>
        </button>
        <button className="cstat-card" onClick={() => onNavigate?.('paymenthistory')}>
          <div className="cstat-icon-wrap cstat-purple">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
            </svg>
          </div>
          <div>
            <div className="cstat-number">₱{stats.totalSpent.toFixed(0)}</div>
            <div className="cstat-label">Total Spent</div>
          </div>
        </button>
        <div className="cstat-card">
          <div className="cstat-icon-wrap cstat-blue">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/>
            </svg>
          </div>
          <div>
            <div className="cstat-number">{services.length}</div>
            <div className="cstat-label">Services</div>
          </div>
        </div>
      </div>

      {/* Service Browse */}
      <div className="page-section">
        <div className="services-header">
          <div>
            <h2 className="section-title">Browse Services</h2>
            <p className="section-sub">Find the perfect look from our talented artists</p>
          </div>
        </div>
        <div className="service-search-container">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="search-icon">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
          </svg>
          <input
            type="text"
            placeholder="Search services, artists, or categories..."
            value={searchTerm}
            onChange={handleSearch}
            className="service-search-input"
          />
        </div>

        {/* Category filter chips */}
        <div className="feed-category-filter">
          {FEED_CATEGORIES.map(cat => (
            <button
              key={cat}
              className={`feed-cat-btn${activeCategory === cat ? ' active' : ''}`}
              onClick={() => handleCategoryFilter(cat)}
            >
              {cat}
            </button>
          ))}
        </div>

        {loading && <div className="empty-state"><p>Loading services...</p></div>}
        {error && (
          <div className="empty-state error-state">
            <p>Error: {error}</p>
            <button className="btn-secondary" onClick={fetchAll} style={{marginTop:'16px'}}>Retry</button>
          </div>
        )}
        {!loading && filteredServices.length === 0 && (
          <div className="empty-state">
            <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/>
            </svg>
            <p>{searchTerm ? 'No services match your search' : 'No services available'}</p>
          </div>
        )}
        {!loading && filteredServices.length > 0 && (
          <div className="post-feed">
            {filteredServices.map(service => (
              <div key={service.id} className="post-card">
                {/* Post Header - clicking body opens modal with photo 0 */}
                <div className="post-header">
                  <Link to={`/artist/${service.artistId}`} className="post-artist-link" onClick={e => e.stopPropagation()}>
                    <img
                      src={service.artistProfileImage || `https://i.pravatar.cc/48?u=${service.artistId}`}
                      alt={service.artistName}
                      className="post-artist-avatar"
                      onError={e => { e.target.src = `https://i.pravatar.cc/48?u=${service.artistId}`; }}
                    />
                  </Link>
                  <div className="post-header-info">
                    <Link to={`/artist/${service.artistId}`} className="post-artist-name-link">
                      <span className="post-artist-name">{service.artistName || 'Unknown Artist'}</span>
                    </Link>
                    <span className="post-meta">Beauty Artist &bull; GlamSched</span>
                  </div>
                  <button
                    className={`post-follow-btn${service.followedByMe ? ' following' : ''}`}
                    onClick={e => handleFollow(e, service)}
                  >
                    {service.followedByMe ? '✓ Following' : '+ Follow'}
                  </button>
                  {service.price != null && (
                    <span className="post-price-badge">₱{service.price.toFixed(2)}</span>
                  )}
                </div>

                {/* Post Body */}
                <div className="post-body" onClick={() => { setSelectedService(service); setActivePhoto(0); }}>
                  <p className="post-service-title">{service.name}</p>
                  {service.description && (
                    <p className="post-description">{service.description}</p>
                  )}
                </div>

                {/* Post Photo Carousel */}
                {service.photos && service.photos.length > 0 && (
                  <div className="post-photo-wrap post-photo-carousel">
                    <img
                      src={service.photos[photoIndexes[service.id] || 0]}
                      alt={service.name}
                      className="post-photo"
                      onClick={() => { setSelectedService(service); setActivePhoto(photoIndexes[service.id] || 0); }}
                    />
                    {service.photos.length > 1 && (
                      <>
                        <button className="post-carousel-btn post-carousel-prev" onClick={e => photoPrev(e, service)}>&#8249;</button>
                        <button className="post-carousel-btn post-carousel-next" onClick={e => photoNext(e, service)}>&#8250;</button>
                        <div className="post-carousel-dots">
                          {service.photos.map((_, i) => (
                            <span
                              key={i}
                              className={`post-carousel-dot${(photoIndexes[service.id] || 0) === i ? ' active' : ''}`}
                              onClick={e => { e.stopPropagation(); setPhotoIndexes(prev => ({ ...prev, [service.id]: i })); }}
                            />
                          ))}
                        </div>
                      </>
                    )}
                  </div>
                )}

                {/* Reaction count */}
                {service.reactionCount > 0 && (
                  <div className="post-reaction-summary">
                    <span className="post-reaction-heart-icon">❤️</span>
                    <span className="post-reaction-count">{service.reactionCount}</span>
                  </div>
                )}

                {/* Action Bar */}
                <div className="post-actions">
                  <button
                    className={`post-action-btn${service.likedByMe ? ' liked' : ''}`}
                    onClick={e => handleReact(e, service)}
                  >
                    {service.likedByMe ? '❤️' : '🤍'} Love
                  </button>
                  <button
                    className="post-action-btn post-book-btn"
                    onClick={e => { e.stopPropagation(); handleBookService(service); }}
                  >
                    📅 Book Now
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Service Detail Modal */}
      {selectedService && (
        <div className="svc-modal-overlay" onClick={() => setSelectedService(null)}>
          <div className="svc-modal" onClick={e => e.stopPropagation()}>
            <button className="svc-modal-close" onClick={() => setSelectedService(null)}>✕</button>
            {selectedService.photos && selectedService.photos.length > 0 && (
              <div className="svc-modal-photo">
                <img
                  src={selectedService.photos[activePhoto]}
                  alt={selectedService.name}
                  className="svc-modal-photo-img"
                />
                {selectedService.photos.length > 1 && (
                  <div className="svc-modal-photo-thumbs">
                    {selectedService.photos.map((p, i) => (
                      <img
                        key={i}
                        src={p}
                        alt={`thumbnail ${i + 1}`}
                        className={`svc-modal-thumb${activePhoto === i ? ' active' : ''}`}
                        onClick={() => setActivePhoto(i)}
                      />
                    ))}
                  </div>
                )}
              </div>
            )}
            <div className="svc-modal-artist-row">
              <img
                src={selectedService.artistProfileImage || `https://i.pravatar.cc/60?u=${selectedService.artistId}`}
                alt={selectedService.artistName}
                className="svc-modal-artist-avatar"
                onError={e => { e.target.src = `https://i.pravatar.cc/60?u=${selectedService.artistId}`; }}
              />
              <div>
                <div className="svc-modal-artist-name">{selectedService.artistName || 'Unknown Artist'}</div>
                <div className="svc-modal-artist-label">Artist</div>
              </div>
            </div>
            <h2 className="svc-modal-title">{selectedService.name}</h2>
            {selectedService.description && (
              <p className="svc-modal-description">{selectedService.description}</p>
            )}
            <div className="svc-modal-details">
              <div className="svc-modal-detail-item">
                <span className="svc-modal-detail-label">Price</span>
                <span className="svc-modal-detail-value svc-modal-price">₱{selectedService.price?.toFixed(2) || '0.00'}</span>
              </div>
              {selectedService.duration && (
                <div className="svc-modal-detail-item">
                  <span className="svc-modal-detail-label">Arrive Early</span>
                  <span className="svc-modal-detail-value">15 min before</span>
                </div>
              )}
              {selectedService.category && (
                <div className="svc-modal-detail-item">
                  <span className="svc-modal-detail-label">Category</span>
                  <span className="svc-modal-detail-value">{selectedService.category}</span>
                </div>
              )}
            </div>
            <button className="btn-book-service" style={{marginTop:'24px'}} onClick={() => { setSelectedService(null); handleBookService(selectedService); }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 5v14"/><path d="M5 12h14"/>
              </svg>
              Book This Service
            </button>
            <button
              className={`svc-modal-react-btn${selectedService.likedByMe ? ' liked' : ''}`}
              onClick={e => handleReact(e, selectedService)}
            >
              {selectedService.likedByMe ? '❤️' : '🤍'} {selectedService.likedByMe ? 'Loved' : 'Love this'} {selectedService.reactionCount > 0 ? `· ${selectedService.reactionCount}` : ''}
            </button>
          </div>
        </div>
      )}
    </>
  );
}


// ARTIST Dashboard - Welcome + Stats
// eslint-disable-next-line react/prop-types
function ArtistDashboard({ onNavigate }) {
  const userName = localStorage.getItem('userName') || 'Artist';
  const artistId = localStorage.getItem('userId');
  const [stats, setStats] = useState({ services: 0, pending: 0, confirmed: 0, earnings: 0 });

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [svcData, apptData, pmtData] = await Promise.all([
          apiGet(`/api/services?artistId=${artistId}`).catch(() => []),
          apiGet(`/api/appointments/artist/${artistId}`).catch(() => []),
          apiGet(`/api/payments?artistId=${artistId}`).catch(() => []),
        ]);
        const services = Array.isArray(svcData) ? svcData : (svcData?.data || []);
        const appts = Array.isArray(apptData) ? apptData : (apptData?.data || []);
        const pmts = Array.isArray(pmtData) ? pmtData : (pmtData?.data || []);
        setStats({
          services: services.length,
          pending: appts.filter(a => a.status?.toUpperCase() === 'PENDING').length,
          confirmed: appts.filter(a => ['CONFIRMED'].includes(a.status?.toUpperCase())).length,
          earnings: pmts
            .filter(p => p.status?.toUpperCase() === 'COMPLETED')
            .reduce((sum, p) => sum + (Number.parseFloat(p.amount) || 0), 0),
        });
      } catch { /* ignore */ }
    };
    if (artistId) fetchStats();
  }, [artistId]);

  return (
    <>
      <div className="client-welcome-banner">
        <div className="client-welcome-content">
          <div className="client-welcome-text">
            <h2>Welcome back, <span className="welcome-name">{userName}</span> ✨</h2>
            <p>Manage your services, bookings, and grow your beauty business.</p>
          </div>
        </div>
      </div>

      <div className="client-stats-row">
        <button className="cstat-card" onClick={() => onNavigate?.('myservices')}>
          <div className="cstat-icon-wrap cstat-gold">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/>
            </svg>
          </div>
          <div>
            <div className="cstat-number">{stats.services}</div>
            <div className="cstat-label">My Services</div>
          </div>
        </button>
        <button className="cstat-card" onClick={() => onNavigate?.('appointments')}>
          <div className="cstat-icon-wrap cstat-purple">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
            </svg>
          </div>
          <div>
            <div className="cstat-number">{stats.pending}</div>
            <div className="cstat-label">Pending</div>
          </div>
        </button>
        <button className="cstat-card" onClick={() => onNavigate?.('appointments')}>
          <div className="cstat-icon-wrap cstat-green">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </div>
          <div>
            <div className="cstat-number">{stats.confirmed}</div>
            <div className="cstat-label">Confirmed</div>
          </div>
        </button>
        <div className="cstat-card">
          <div className="cstat-icon-wrap cstat-blue">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
            </svg>
          </div>
          <div>
            <div className="cstat-number">₱{stats.earnings.toFixed(0)}</div>
            <div className="cstat-label">Earnings</div>
          </div>
        </div>
      </div>

      <div className="db-section-heading" style={{marginBottom:'16px', fontSize:'16px', fontWeight:600, color:'#333'}}>Quick Actions</div>
      <div className="artist-quick-actions">
        <button className="artist-action-card" onClick={() => onNavigate?.('myservices')}>
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/>
          </svg>
          <h4>My Services</h4>
          <p>Add or edit your offerings</p>
        </button>
        <button className="artist-action-card" onClick={() => onNavigate?.('appointments')}>
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
          <h4>Manage Bookings</h4>
          <p>Approve or decline requests</p>
        </button>
        <button className="artist-action-card" onClick={() => onNavigate?.('settings')}>
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <circle cx="12" cy="12" r="3"/>
            <path d="M12 1v6M12 17v6M1 12h6M17 12h6"/>
          </svg>
          <h4>Settings</h4>
          <p>Update profile &amp; password</p>
        </button>
      </div>
    </>
  );
}

const artistNavItems = [
  {
    key: 'overview',
    label: 'Dashboard',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
        <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
      </svg>
    ),
  },
  {
    key: 'appointments',
    label: 'Manage Bookings',
    group: 'BOOKINGS',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/>
        <line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
      </svg>
    ),
  },
  {
    key: 'myservices',
    label: 'My Services',
    group: 'SERVICES',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/>
      </svg>
    ),
  },
  {
    key: 'portfolio',
    label: 'Portfolio',
    group: 'SERVICES',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
      </svg>
    ),
  },
  {
    key: 'profile',
    label: 'Profile',
    group: 'ACCOUNT',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
      </svg>
    ),
  },
  {
    key: 'settings',
    label: 'Settings',
    group: 'ACCOUNT',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="3"/>
        <path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83"/>
      </svg>
    ),
  },
];

// FAVORITES component
function FavoritesPage() {
  const navigate = useNavigate();
  const [favorites, setFavorites] = useState([]);

  useEffect(() => {
    const stored = JSON.parse(localStorage.getItem('favoriteServices') || '[]');
    setFavorites(stored);
  }, []);

  const removeFavorite = (serviceId) => {
    const updated = favorites.filter(s => s.id !== serviceId);
    setFavorites(updated);
    localStorage.setItem('favoriteServices', JSON.stringify(updated));
  };

  const handleBook = (service) => {
    localStorage.setItem('selectedService', JSON.stringify(service));
    navigate('/dashboard/booknow');
  };

  if (favorites.length === 0) {
    return (
      <div className="page-section">
        <h2 className="section-title">Favorites</h2>
        <p className="section-sub">Services you love, saved for quick booking</p>
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#d4af37" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
          </svg>
          <p>No favorites yet. Heart a service from the dashboard!</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-section">
      <h2 className="section-title">Favorites</h2>
      <p className="section-sub">Services you love, saved for quick booking</p>
      <div className="fav-grid">
        {favorites.map(service => (
          <div key={service.id} className="fav-card">
            {service.photos && service.photos.length > 0 && (
              <div className="fav-photo-wrap">
                <img src={service.photos[0]} alt={service.name} className="fav-photo" />
                {service.photos.length > 1 && (
                  <span className="fav-more-photos">+{service.photos.length - 1} more</span>
                )}
                <button className="fav-heart-btn active" onClick={() => removeFavorite(service.id)} title="Remove from favorites">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                  </svg>
                </button>
              </div>
            )}
            {(!service.photos || service.photos.length === 0) && (
              <button className="fav-heart-btn active fav-heart-no-photo" onClick={() => removeFavorite(service.id)} title="Remove from favorites">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
              </button>
            )}
            <div className="fav-body">
              <h3 className="fav-name">{service.name}</h3>
              <p className="fav-artist">by {service.artistName}</p>
              <p className="fav-desc">{service.description}</p>
              <div className="fav-footer">
                <span className="fav-price">₱{service.price?.toFixed(2)}</span>
                <button className="fav-book-btn" onClick={() => handleBook(service)}>Book Now</button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function getStrength(pw) {
  if (pw.length >= 10 && /[A-Z]/.test(pw) && /\d/.test(pw)) return 'strong';
  if (pw.length >= 6) return 'medium';
  return 'weak';
}

function SettingsPage() {
  const userId = localStorage.getItem('userId');
  const userRole = localStorage.getItem('userRole') || 'CLIENT';
  const [profile, setProfile] = useState({ name: '', email: '', phone: '', bio: '' });
  const [profileSnapshot, setProfileSnapshot] = useState(null);
  const [editingProfile, setEditingProfile] = useState(false);
  const [profileMsg, setProfileMsg] = useState(null);
  const [profileSaving, setProfileSaving] = useState(false);

  const [passwords, setPasswords] = useState({ current: '', next: '', confirm: '' });
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [pwMsg, setPwMsg] = useState(null);
  const [pwSaving, setPwSaving] = useState(false);

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletePassword, setDeletePassword] = useState('');
  const [deleteConfirmText, setDeleteConfirmText] = useState('');
  const [deleteMsg, setDeleteMsg] = useState(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (!userId) return;
    apiGet(`/api/users/${userId}`).then(data => {
      const u = data?.data || data;
      if (u) {
        const p = { name: u.name || '', email: u.email || '', phone: u.phone || '', bio: u.bio || '' };
        setProfile(p);
        setProfileSnapshot(p);
      }
    }).catch(() => {});
  }, [userId]);

  const saveProfile = async () => {
    setProfileSaving(true);
    setProfileMsg(null);
    try {
      const res = await apiPut(`/api/users/${userId}`, profile);
      if (res?.data || res?.success) {
        localStorage.setItem('userName', profile.name);
        setProfileSnapshot(profile);
        setEditingProfile(false);
        setProfileMsg({ ok: true, text: 'Profile updated successfully!' });
        setTimeout(() => setProfileMsg(null), 3000);
      } else {
        setProfileMsg({ ok: false, text: res?.message || 'Failed to update profile' });
      }
    } catch (e) {
      setProfileMsg({ ok: false, text: e.message || 'Error saving profile' });
    } finally {
      setProfileSaving(false);
    }
  };

  const savePassword = async () => {
    if (!passwords.current) { setPwMsg({ ok: false, text: 'Enter your current password' }); return; }
    if (passwords.next.length < 6) { setPwMsg({ ok: false, text: 'New password must be at least 6 characters' }); return; }
    if (passwords.next !== passwords.confirm) { setPwMsg({ ok: false, text: 'New passwords do not match' }); return; }
    setPwSaving(true);
    setPwMsg(null);
    try {
      const res = await apiPut(`/api/users/${userId}/password`, {
        currentPassword: passwords.current,
        newPassword: passwords.next,
      });
      if (res?.status === 'SUCCESS' || res?.data) {
        setPwMsg({ ok: true, text: 'Password changed successfully!' });
        setPasswords({ current: '', next: '', confirm: '' });
        setTimeout(() => setPwMsg(null), 3000);
      } else {
        setPwMsg({ ok: false, text: res?.message || 'Failed to change password' });
      }
    } catch (e) {
      setPwMsg({ ok: false, text: e.message || 'Incorrect current password' });
    } finally {
      setPwSaving(false);
    }
  };

  const initial = profile.name?.charAt(0)?.toUpperCase() || '?';

  return (
    <div className="st-root">
      {/* Page Header */}
      <div className="st-page-header">
        <div className="st-page-avatar">{initial}</div>
        <div>
          <h2 className="st-page-title">{profile.name || 'Your Account'}</h2>
          <p className="st-page-sub">{userRole === 'ARTIST' ? 'Beauty Artist' : 'Client'} · {profile.email}</p>
        </div>
      </div>

      <div className="st-layout">
        {/* Left column */}
        <div className="st-col-main">

          {/* Profile Information */}
          <div className="st-card">
            <div className="st-card-header">
              <div className="st-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              </div>
              <div style={{flex:1}}>
                <h3 className="st-card-title">Profile Information</h3>
                <p className="st-card-sub">Update your name, email, and contact details</p>
              </div>
              {!editingProfile && (
                <button className="st-edit-btn" onClick={() => { setProfileSnapshot(profile); setEditingProfile(true); }}>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  Edit
                </button>
              )}
            </div>

            {editingProfile ? (
              <>
                <div className="st-fields-grid">
                  <div className="st-field">
                    <label htmlFor="st-name">Full Name</label>
                    <input id="st-name" type="text" value={profile.name} onChange={e => setProfile(p => ({ ...p, name: e.target.value }))} placeholder="Your full name" />
                  </div>
                  <div className="st-field">
                    <label htmlFor="st-email">Email Address</label>
                    <input id="st-email" type="email" value={profile.email} onChange={e => setProfile(p => ({ ...p, email: e.target.value }))} placeholder="your@email.com" />
                  </div>
                  <div className="st-field">
                    <label htmlFor="st-phone">Phone Number</label>
                    <input id="st-phone" type="tel" value={profile.phone} onChange={e => setProfile(p => ({ ...p, phone: e.target.value }))} placeholder="+63 912 345 6789" />
                  </div>
                  <div className="st-field st-field-full">
                    <label htmlFor="st-bio">Bio</label>
                    <textarea id="st-bio" value={profile.bio} onChange={e => setProfile(p => ({ ...p, bio: e.target.value }))} placeholder={userRole === 'ARTIST' ? 'Describe your services and expertise…' : 'Tell a little about yourself…'} rows={3} maxLength={300} />
                    <span className="st-char-count">{profile.bio.length}/300</span>
                  </div>
                </div>
                {profileMsg && <div className={`st-msg${profileMsg.ok ? ' ok' : ' err'}`}>{profileMsg.ok ? '✓' : '✕'} {profileMsg.text}</div>}
                <div className="st-card-footer">
                  <button className="st-btn-primary" onClick={saveProfile} disabled={profileSaving}>
                    {profileSaving ? <><span className="st-spinner" /> Saving…</> : 'Save Changes'}
                  </button>
                  <button className="st-btn-cancel" onClick={() => { setProfile(profileSnapshot); setEditingProfile(false); setProfileMsg(null); }}>Cancel</button>
                </div>
              </>
            ) : (
              <>
                <div className="st-readonly-grid">
                  <div className="st-readonly-item">
                    <span className="st-readonly-label">Full Name</span>
                    <span className="st-readonly-value">{profile.name || '—'}</span>
                  </div>
                  <div className="st-readonly-item">
                    <span className="st-readonly-label">Email Address</span>
                    <span className="st-readonly-value">{profile.email || '—'}</span>
                  </div>
                  <div className="st-readonly-item">
                    <span className="st-readonly-label">Phone Number</span>
                    <span className="st-readonly-value">{profile.phone || '—'}</span>
                  </div>
                  <div className="st-readonly-item st-readonly-full">
                    <span className="st-readonly-label">Bio</span>
                    <span className="st-readonly-value">{profile.bio || '—'}</span>
                  </div>
                </div>
                {profileMsg && <div className={`st-msg${profileMsg.ok ? ' ok' : ' err'}`}>{profileMsg.ok ? '✓' : '✕'} {profileMsg.text}</div>}
              </>
            )}
          </div>

          {/* Change Password */}
          <div className="st-card">
            <div className="st-card-header">
              <div className="st-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              </div>
              <div>
                <h3 className="st-card-title">Change Password</h3>
                <p className="st-card-sub">Keep your account secure with a strong password</p>
              </div>
            </div>

            <div className="st-fields-grid">
              <div className="st-field st-field-full">
                <label htmlFor="st-pw-cur">Current Password</label>
                <div className="st-pw-wrap">
                  <input id="st-pw-cur" type={showCurrent ? 'text' : 'password'} value={passwords.current} onChange={e => setPasswords(p => ({ ...p, current: e.target.value }))} placeholder="Enter current password" />
                  <button type="button" className="st-pw-eye" onClick={() => setShowCurrent(v => !v)}>
                    {showCurrent
                      ? <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                      : <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                    }
                  </button>
                </div>
              </div>
              <div className="st-field">
                <label htmlFor="st-pw-new">New Password</label>
                <div className="st-pw-wrap">
                  <input id="st-pw-new" type={showNew ? 'text' : 'password'} value={passwords.next} onChange={e => setPasswords(p => ({ ...p, next: e.target.value }))} placeholder="At least 6 characters" />
                  <button type="button" className="st-pw-eye" onClick={() => setShowNew(v => !v)}>
                    {showNew
                      ? <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                      : <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                    }
                  </button>
                </div>
                {passwords.next && (
                  <div className="st-pw-strength">
                    <div className={`st-pw-bar ${getStrength(passwords.next)}`} />
                    <span>{getStrength(passwords.next).charAt(0).toUpperCase() + getStrength(passwords.next).slice(1)}</span>
                  </div>
                )}
              </div>
              <div className="st-field">
                <label htmlFor="st-pw-conf">Confirm New Password</label>
                <div className="st-pw-wrap">
                  <input id="st-pw-conf" type={showConfirm ? 'text' : 'password'} value={passwords.confirm} onChange={e => setPasswords(p => ({ ...p, confirm: e.target.value }))} placeholder="Repeat new password" />
                  <button type="button" className="st-pw-eye" onClick={() => setShowConfirm(v => !v)}>
                    {showConfirm
                      ? <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                      : <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                    }
                  </button>
                </div>
                {passwords.confirm && passwords.next !== passwords.confirm && (
                  <span className="st-field-err">Passwords do not match</span>
                )}
                {passwords.confirm && passwords.next === passwords.confirm && passwords.next.length >= 6 && (
                  <span className="st-field-ok">Passwords match</span>
                )}
              </div>
            </div>

            {pwMsg && <div className={`st-msg${pwMsg.ok ? ' ok' : ' err'}`}>{pwMsg.ok ? '✓' : '✕'} {pwMsg.text}</div>}

            <div className="st-card-footer">
              <button className="st-btn-primary" onClick={savePassword} disabled={pwSaving}>
                {pwSaving ? <><span className="st-spinner" /> Updating…</> : 'Update Password'}
              </button>
            </div>
          </div>
        </div>

        {/* Right column */}
        <div className="st-col-side">
          {/* Account Info */}
          <div className="st-card">
            <h3 className="st-card-title" style={{marginBottom:'16px'}}>Account Info</h3>
            <div className="st-info-item">
              <span className="st-info-label">Account Type</span>
              <span className={`st-role-badge ${userRole === 'ARTIST' ? 'artist' : 'client'}`}>{userRole === 'ARTIST' ? 'Beauty Artist' : 'Client'}</span>
            </div>
            <div className="st-info-item">
              <span className="st-info-label">Member Since</span>
              <span className="st-info-value">May 2026</span>
            </div>
            <div className="st-info-item">
              <span className="st-info-label">Status</span>
              <span className="st-status-dot">Active</span>
            </div>
          </div>

          {/* Tips */}
          <div className="st-card st-tips-card">
            <h3 className="st-card-title" style={{marginBottom:'12px'}}>Security Tips</h3>
            <ul className="st-tips-list">
              <li>Use a mix of letters, numbers &amp; symbols</li>
              <li>Don&apos;t reuse passwords across sites</li>
              <li>Keep your email address up to date</li>
              <li>Log out on shared devices</li>
            </ul>
          </div>

          {/* Danger Zone */}
          <div className="st-card st-danger-card">
            <h3 className="st-danger-title">Danger Zone</h3>
            <p className="st-danger-sub">Once deleted, your account and all data cannot be recovered.</p>
            <button className="st-btn-danger" onClick={() => setShowDeleteModal(true)}>
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
              Delete My Account
            </button>
          </div>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className="st-modal-overlay" onClick={() => { setShowDeleteModal(false); setDeletePassword(''); setDeleteConfirmText(''); setDeleteMsg(null); }}>
          <div className="st-modal" onClick={e => e.stopPropagation()}>
            <div className="st-modal-header">
              <div className="st-modal-icon-danger">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              </div>
              <div>
                <h3 className="st-modal-title">Delete Account</h3>
                <p className="st-modal-sub">This action is permanent and cannot be undone.</p>
              </div>
            </div>

            <div className="st-modal-body">
              <p className="st-modal-warning">This will permanently delete:</p>
              <ul className="st-modal-list">
                <li>Your profile and account data</li>
                <li>All your appointments and bookings</li>
                <li>All services you created (if artist)</li>
                <li>All reviews and reactions</li>
              </ul>

              <div className="st-field" style={{marginBottom:'14px'}}>
                <label htmlFor="del-confirm">Type <strong>DELETE</strong> to confirm</label>
                <input
                  id="del-confirm"
                  type="text"
                  value={deleteConfirmText}
                  onChange={e => setDeleteConfirmText(e.target.value)}
                  placeholder="DELETE"
                  autoComplete="off"
                />
              </div>
              <div className="st-field">
                <label htmlFor="del-pw">Enter your password</label>
                <input
                  id="del-pw"
                  type="password"
                  value={deletePassword}
                  onChange={e => setDeletePassword(e.target.value)}
                  placeholder="Your current password"
                />
              </div>
              {deleteMsg && <div className={`st-msg${deleteMsg.ok ? ' ok' : ' err'}`} style={{marginTop:'12px'}}>{deleteMsg.ok ? '✓' : '✕'} {deleteMsg.text}</div>}
            </div>

            <div className="st-modal-footer">
              <button
                className="st-btn-danger-confirm"
                disabled={deleteConfirmText !== 'DELETE' || !deletePassword || deleting}
                onClick={async () => {
                  setDeleting(true); setDeleteMsg(null);
                  try {
                    const res = await apiDelete(`/api/users/${userId}?password=${encodeURIComponent(deletePassword)}`);
                    if (res?.status === 'SUCCESS' || res?.data) {
                      setDeleteMsg({ ok: true, text: 'Account deleted. Redirecting…' });
                      setTimeout(() => {
                        localStorage.clear();
                        window.location.href = '/';
                      }, 1500);
                    } else {
                      setDeleteMsg({ ok: false, text: res?.message || 'Failed to delete account' });
                    }
                  } catch (e) {
                    setDeleteMsg({ ok: false, text: e.message || 'Incorrect password' });
                  } finally { setDeleting(false); }
                }}
              >
                {deleting ? <><span className="st-spinner" /> Deleting…</> : 'Delete My Account'}
              </button>
              <button className="st-btn-cancel" onClick={() => { setShowDeleteModal(false); setDeletePassword(''); setDeleteConfirmText(''); setDeleteMsg(null); }}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function ArtistPortfolio() {
  const artistId = localStorage.getItem('userId');
  const artistName = localStorage.getItem('userName') || 'Artist';
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedPhoto, setSelectedPhoto] = useState(null);

  useEffect(() => {
    const load = async () => {
      try {
        const data = await apiGet(`/api/services?artistId=${artistId}`);
        const list = Array.isArray(data) ? data : (data.data || []);
        setServices(list);
      } catch { /* ignore */ }
      finally { setLoading(false); }
    };
    if (artistId) load();
  }, [artistId]);

  const allPhotos = services.flatMap(s =>
    (s.photos || []).map(photo => ({ photo, serviceName: s.name, price: s.price, description: s.description }))
  );

  return (
    <div className="page-section">
      <div className="services-header">
        <div>
          <h2 className="section-title">Portfolio</h2>
          <p className="section-sub">Showcase your work — {allPhotos.length} photo{allPhotos.length !== 1 ? 's' : ''} from {services.length} service{services.length !== 1 ? 's' : ''}</p>
        </div>
      </div>

      {loading && <div className="empty-state"><p>Loading portfolio...</p></div>}

      {!loading && allPhotos.length === 0 && (
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
          <p>No photos yet. Add photos to your services to build your portfolio!</p>
        </div>
      )}

      {!loading && allPhotos.length > 0 && (
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

      {/* Lightbox Modal */}
      {selectedPhoto && (
        <div className="pf-lightbox" onClick={() => setSelectedPhoto(null)}>
          <div className="pf-lightbox-content" onClick={e => e.stopPropagation()}>
            <button className="pf-lightbox-close" onClick={() => setSelectedPhoto(null)}>✕</button>
            <img src={selectedPhoto.photo} alt={selectedPhoto.serviceName} className="pf-lightbox-img" />
            <div className="pf-lightbox-info">
              <h3>{selectedPhoto.serviceName}</h3>
              {selectedPhoto.description && <p className="pf-lightbox-desc">{selectedPhoto.description}</p>}
              {selectedPhoto.price != null && <span className="pf-lightbox-price">₱{selectedPhoto.price.toFixed(2)}</span>}
              <p className="pf-lightbox-artist">by {artistName}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// eslint-disable-next-line react/prop-types
function PageContent({ active, onSetActive, userRole }) {
  if (active === 'booknow') {
    return <BookingFlow />;
  }
  if (active === 'myappointments') {
    return <MyAppointments />;
  }
  if (active === 'favorites') {
    return <FavoritesPage />;
  }
  if (active === 'paymenthistory') {
    return <PaymentHistory />;
  }
  if (active === 'appointments') {
    // For ARTIST: show managed appointments, For CLIENT: show booking form
    return userRole === 'ARTIST' ? <ArtistAppointments /> : <BookAppointment />;
  }
  if (active === 'myservices') {
    return <ArtistServices />;
  }
  if (active === 'portfolio') {
    return <ArtistPortfolio />;
  }
  if (active === 'settings') {
    return <SettingsPage />;
  }
  if (active === 'profile') {
    return <UserProfile />;
  }

  // Default: overview - Different for CLIENT vs ARTIST
  if (userRole === 'ARTIST') {
    return <ArtistDashboard onNavigate={onSetActive} />;
  }

  // CLIENT Overview - Welcome + Stats + Browse
  return <ClientDashboard onNavigate={onSetActive} />;
}

function Dashboard() {
  const navigate = useNavigate();
  const { tab, artistId } = useParams();
  const isPaymentRoute = window.location.pathname === '/payment';
  const [active, setActive] = useState(isPaymentRoute ? 'booknow' : (tab || 'overview'));
  const userRole = localStorage.getItem('userRole') || 'CLIENT';
  const [displayName, setDisplayName] = useState(localStorage.getItem('userName') || 'User');
  const [sidebarAvatar, setSidebarAvatar] = useState(null);

  useEffect(() => {
    const userId = localStorage.getItem('userId');
    if (!userId) return;
    apiGet(`/api/users/${userId}`)
      .then(data => {
        const u = data?.data || data;
        const name = u?.name || u?.fullName;
        if (name) {
          setDisplayName(name);
          localStorage.setItem('userName', name);
        }
        if (u?.profileImage) setSidebarAvatar(u.profileImage);
      })
      .catch(() => {});
  }, []);
  
  // Update active state when URL tab changes
  useEffect(() => {
    if (tab) {
      setActive(tab);
    } else {
      setActive('overview');
    }
  }, [tab]);
  
  // Use appropriate navigation based on role
  const navItems = userRole === 'ARTIST' ? artistNavItems : clientNavItems;

  const handleLogout = () => {
    // Clear user data and redirect to login
    localStorage.removeItem('userId');
    localStorage.removeItem('userName');
    localStorage.removeItem('userRole');
    navigate('/login');
  };

  const activeLabel = isPaymentRoute ? 'Payment' : artistId ? 'Artist Profile' : (navItems.find(n => n.key === active)?.label || 'Dashboard');

  return (
    <div className="dashboard-layout">
      {/* ── Sidebar ── */}
      <aside className="db-sidebar">
        <div className="db-sidebar-brand">
          <span className="db-brand-name">GlamSched</span>
        </div>
        <div className="db-sidebar-user">
          <div className="db-sidebar-avatar">
            {sidebarAvatar
              ? <img src={sidebarAvatar} alt={displayName} style={{width:'100%',height:'100%',objectFit:'cover',borderRadius:'50%'}} />
              : displayName.charAt(0).toUpperCase()
            }
          </div>
          <div className="db-sidebar-userinfo">
            <span className="db-sidebar-username">{displayName}</span>
            <span className="db-sidebar-userrole">{userRole === 'ARTIST' ? 'Artist' : 'Client'}</span>
          </div>
        </div>
        <nav className="db-sidebar-nav">
          {navItems.reduce((acc, item, index) => {
            const prev = navItems[index - 1];
            const showGroup = item.group && (!prev || prev.group !== item.group);
            if (showGroup) {
              acc.push(<div key={`g-${item.group}`} className="db-nav-section">{item.group}</div>);
            }
            acc.push(
              <button
                key={item.key}
                className={`db-nav-item${active === item.key ? ' active' : ''}${item.highlight ? ' db-nav-highlight' : ''}`}
                onClick={() => {
                  if (item.key === 'booknow') {
                    navigate('/dashboard/booknow');
                  } else if (item.key === 'overview') {
                    navigate('/dashboard');
                  } else {
                    navigate(`/dashboard/${item.key}`);
                  }
                }}
              >
                <span className="db-nav-icon">{item.icon}</span>
                <span>{item.label}</span>
              </button>
            );
            return acc;
          }, [])}
        </nav>
        <div className="db-sidebar-footer">
          <button className="db-signout-btn" onClick={handleLogout}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            Sign Out
          </button>
        </div>
      </aside>

      {/* ── Main area ── */}
      <main className="db-main">
        <header className="db-topbar">
          <div className="db-topbar-left">
            <h1 className="db-page-title">{activeLabel}</h1>
          </div>
          <div className="db-topbar-right">
            <span className="db-topbar-date">
              {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })}
            </span>
          </div>
        </header>
        <div className="db-content">
          {isPaymentRoute
            ? <PaymentPage />
            : artistId
              ? <ArtistProfile />
              : <PageContent active={active} onSetActive={setActive} userRole={userRole} />
          }
        </div>
      </main>
    </div>
  );
}

export default Dashboard;

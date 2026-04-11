import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import BookAppointment from './BookAppointment';
import ArtistAppointments from './ArtistAppointments';
import UserProfile from './UserProfile';
import MyAppointments from './MyAppointments';
import PaymentHistory from './PaymentHistory';
import AddServiceModal from '../components/AddServiceModal';
import './Dashboard.css';
import { apiGet } from '../utils/api';

// Artist Services Component with Search Filter
function ArtistServices() {
  const [services, setServices] = useState([]);
  const [filteredServices, setFilteredServices] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showAddServiceModal, setShowAddServiceModal] = useState(false);
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

  const handleDeleteService = (service) => {
    // eslint-disable-next-line no-restricted-globals
    if (window.confirm('Delete service: ' + service.name + '?')) {
      setServices(services.filter(s => s.id !== service.id));
      setFilteredServices(filteredServices.filter(s => s.id !== service.id));
    }
  };

  const handleServiceAdded = () => {
    // Refresh services list
    fetchServices();
  };

  // Handle search filter
  const handleSearch = (e) => {
    const term = e.target.value.toLowerCase();
    setSearchTerm(term);
    
    const filtered = services.filter(service =>
      service.name.toLowerCase().includes(term) ||
      (service.description && service.description.toLowerCase().includes(term))
    );
    setFilteredServices(filtered);
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

      {/* Search Filter */}
      <div className="service-search-container">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="search-icon">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
        </svg>
        <input
          type="text"
          placeholder="Search services by name..."
          value={searchTerm}
          onChange={handleSearch}
          className="service-search-input"
        />
      </div>

      {loading && (
        <div className="empty-state">
          <p>Loading services...</p>
        </div>
      )}

      {error && (
        <div className="empty-state error-state">
          <p>Error: {error}</p>
          <button 
            className="btn-secondary" 
            onClick={fetchServices}
            style={{marginTop: '16px'}}
          >
            Retry
          </button>
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
        <div className="services-grid">
          {filteredServices.map(service => (
            <div key={service.id} className="service-card">
              <div className="service-card-header">
                <h3 className="service-name">{service.name}</h3>
                <div className="service-badges">
                  {service.duration && <span className="badge duration">{service.duration} min</span>}
                </div>
              </div>
              
              {service.description && (
                <p className="service-description">{service.description}</p>
              )}

              <div className="service-details">
                <div className="detail-item">
                  <span className="detail-label">Price</span>
                  <span className="detail-value">${service.price?.toFixed(2) || '0.00'}</span>
                </div>
                {service.duration && (
                  <div className="detail-item">
                    <span className="detail-label">Duration</span>
                    <span className="detail-value">{service.duration} minutes</span>
                  </div>
                )}
              </div>

              <div className="service-actions">
                <button className="btn-secondary" title="Edit service" onClick={() => alert('Edit service: ' + service.name + '\nFeature coming soon!')}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15H9v-3L18.5 2.5z"/>
                  </svg>
                  Edit
                </button>
                <button className="btn-danger" title="Delete service" onClick={() => handleDeleteService(service)}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                  </svg>
                  Delete
                </button>
              </div>
            </div>
          ))}
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
    label: 'Dashboard',
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
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/>
        <line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
      </svg>
    ),
  },
  {
    key: 'paymenthistory',
    label: 'Payment History',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/>
      </svg>
    ),
  },
  {
    key: 'profile',
    label: 'Profile',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
      </svg>
    ),
  },
  {
    key: 'booknow',
    label: 'Book Now',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"/><path d="M12.5 7H11v6l5.25 3.15.75-1.23-4.5-2.67z"/>
      </svg>
    ),
  },
];

// CLIENT Dashboard - Browse and Book Services
function ClientDashboard() {
  const navigate = useNavigate();
  const [services, setServices] = useState([]);
  const [filteredServices, setFilteredServices] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchServices = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await apiGet('/api/services');
      const serviceList = Array.isArray(data) ? data : (data.data || []);
      setServices(serviceList);
      setFilteredServices(serviceList);
    } catch (err) {
      setError(err.message || 'Failed to load services');
      setServices([]);
      setFilteredServices([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchServices();
  }, []);

  const handleSearch = (e) => {
    const term = e.target.value.toLowerCase();
    setSearchTerm(term);
    
    const filtered = services.filter(service =>
      (service.name && service.name.toLowerCase().includes(term)) ||
      (service.description && service.description.toLowerCase().includes(term)) ||
      (service.artistName && service.artistName.toLowerCase().includes(term))
    );
    setFilteredServices(filtered);
  };

  const handleBookService = (service) => {
    localStorage.setItem('selectedService', JSON.stringify(service));
    navigate('/booking');
  };

  return (
    <div className="page-section">
      <div className="services-header">
        <div>
          <h2 className="section-title">Book a Service</h2>
          <p className="section-sub">Browse and book from our talented artists</p>
        </div>
      </div>

      {/* Search Filter */}
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

      {loading && (
        <div className="empty-state">
          <p>Loading services...</p>
        </div>
      )}

      {error && (
        <div className="empty-state error-state">
          <p>Error: {error}</p>
          <button 
            className="btn-secondary" 
            onClick={fetchServices}
            style={{marginTop: '16px'}}
          >
            Retry
          </button>
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
        <div className="services-grid">
          {filteredServices.map(service => (
            <div key={service.id} className="service-card">
              {service.photos && service.photos.length > 0 && (
                <div className="service-card-photo">
                  <img 
                    src={service.photos[0]} 
                    alt={service.name}
                    className="service-photo"
                  />
                </div>
              )}
              
              <div className="service-card-header">
                <h3 className="service-name">{service.name}</h3>
              </div>
              
              {service.description && (
                <p className="service-description">{service.description}</p>
              )}

              <div className="service-artist-info">
                <span className="artist-label">👨‍🎨 Artist</span>
                <span className="artist-name">{service.artistName || 'Unknown Artist'}</span>
              </div>

              <div className="service-details">
                <div className="detail-item">
                  <span className="detail-label">Price</span>
                  <span className="detail-value">${service.price?.toFixed(2) || '0.00'}</span>
                </div>
              </div>

              <button 
                className="btn-book-service"
                onClick={() => handleBookService(service)}
                title="Book this service"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M12 5v14"/><path d="M5 12h14"/>
                </svg>
                Book Service
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
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
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/>
      </svg>
    ),
  },
  {
    key: 'portfolio',
    label: 'Portfolio',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
      </svg>
    ),
  },
  {
    key: 'settings',
    label: 'Settings',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="3"/>
        <path d="M12 1v6"/>
        <path d="M12 17v6"/>
        <path d="M1 12h6"/>
        <path d="M17 12h6"/>
        <path d="M4.22 4.22l4.24 4.24"/>
        <path d="M15.54 15.54l4.24 4.24"/>
        <path d="M4.22 19.78l4.24-4.24"/>
        <path d="M15.54 8.46l4.24-4.24"/>
      </svg>
    ),
  },
];

function PageContent({ active, onSetActive, userRole }) {
  if (active === 'myappointments') {
    return <MyAppointments />;
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
    return (
      <div className="page-section">
        <h2 className="section-title">Portfolio</h2>
        <p className="section-sub">Showcase your work and reviews</p>
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
          </svg>
          <p>No portfolio items yet</p>
        </div>
      </div>
    );
  }
  if (active === 'settings') {
    return (
      <div className="page-section">
        <h2 className="section-title">Settings</h2>
        <p className="section-sub">Manage your account settings</p>
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="12" cy="12" r="3"/><path d="M12 1v6m0 6v6M4.22 4.22l4.24 4.24m3.08 3.08l4.24 4.24M1 12h6m6 0h6m-16.78 7.78l4.24-4.24m3.08-3.08l4.24-4.24"/>
          </svg>
          <p>Settings</p>
        </div>
      </div>
    );
  }
  if (active === 'profile') {
    return <UserProfile />;
  }

  // Default: overview - Different for CLIENT vs ARTIST
  if (userRole === 'ARTIST') {
    return (
      <>
        <div className="db-welcome-banner artist-banner">
          <div className="db-welcome-text">
            <h2>Welcome back, {localStorage.getItem('userName')}</h2>
            <p>Manage your services and appointments</p>
          </div>
        </div>
        <div className="db-stats-grid">
          <div className="db-stat-card">
            <span className="db-stat-icon">💅</span>
            <div>
              <div className="db-stat-number">0</div>
              <div className="db-stat-label">Total Services</div>
            </div>
          </div>
          <div className="db-stat-card">
            <span className="db-stat-icon">📅</span>
            <div>
              <div className="db-stat-number">0</div>
              <div className="db-stat-label">Upcoming Bookings</div>
            </div>
          </div>
          <div className="db-stat-card">
            <span className="db-stat-icon">⭐</span>
            <div>
              <div className="db-stat-number">0</div>
              <div className="db-stat-label">Client Reviews</div>
            </div>
          </div>
          <div className="db-stat-card">
            <span className="db-stat-icon">💰</span>
            <div>
              <div className="db-stat-number">$0.00</div>
              <div className="db-stat-label">Total Earnings</div>
            </div>
          </div>
        </div>
      </>
    );
  }

  // CLIENT Overview - Browse all available services
  return <ClientDashboard />;
}

function Dashboard() {
  const navigate = useNavigate();
  const { tab } = useParams();
  const [active, setActive] = useState(tab || 'overview');
  const userRole = localStorage.getItem('userRole') || 'CLIENT';
  
  // Update active state when URL tab changes
  useEffect(() => {
    if (tab) {
      setActive(tab);
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

  const activeLabel = navItems.find(n => n.key === active)?.label || 'Dashboard';

  return (
    <div className="dashboard-layout">
      {/* ── Sidebar ── */}
      <aside className="db-sidebar">
        <div className="db-sidebar-brand">
          <span className="db-brand-name">GlamSched</span>
          <span className="db-role-badge">{userRole === 'ARTIST' ? '✨ Artist' : '💅 Client'}</span>
        </div>
        <nav className="db-sidebar-nav">
          {navItems.map(item => (
            <button
              key={item.key}
              className={`db-nav-item${active === item.key ? ' active' : ''}`}
              onClick={() => {
                if (item.key === 'booknow') {
                  navigate('/booking');
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
          ))}
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
          <h1 className="db-page-title">{activeLabel}</h1>
        </header>
        <div className="db-content">
          <PageContent active={active} onSetActive={setActive} userRole={userRole} />
        </div>
      </main>
    </div>
  );
}

export default Dashboard;

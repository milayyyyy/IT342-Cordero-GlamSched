import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const navItems = [
  {
    key: 'overview',
    label: 'Overview',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
        <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
      </svg>
    ),
  },
  {
    key: 'appointments',
    label: 'Appointments',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/>
        <line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
      </svg>
    ),
  },
  {
    key: 'services',
    label: 'Services',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/>
      </svg>
    ),
  },
  {
    key: 'reviews',
    label: 'Reviews',
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
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
];

function PageContent({ active }) {
  if (active === 'appointments') {
    return (
      <div className="page-section">
        <h2 className="section-title">Appointments</h2>
        <p className="section-sub">Manage and view all your upcoming bookings.</p>
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
          <p>No appointments yet</p>
        </div>
      </div>
    );
  }
  if (active === 'services') {
    return (
      <div className="page-section">
        <h2 className="section-title">Services</h2>
        <p className="section-sub">Browse available beauty services.</p>
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/>
          </svg>
          <p>No services available yet</p>
        </div>
      </div>
    );
  }
  if (active === 'reviews') {
    return (
      <div className="page-section">
        <h2 className="section-title">Reviews</h2>
        <p className="section-sub">Share and read beauty experiences.</p>
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
          </svg>
          <p>No reviews yet</p>
        </div>
      </div>
    );
  }
  if (active === 'profile') {
    return (
      <div className="page-section">
        <h2 className="section-title">Profile</h2>
        <p className="section-sub">View and update your personal information.</p>
        <div className="empty-state">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
          </svg>
          <p>No profile data yet</p>
        </div>
      </div>
    );
  }

  // Default: overview
  return (
    <>
      <div className="db-welcome-banner">
        <div className="db-welcome-text">
          <h2>Welcome to GlamSched</h2>
          <p>Your all-in-one beauty scheduling platform</p>
        </div>
      </div>
      <div className="db-stats-grid">
        <div className="db-stat-card">
          <span className="db-stat-icon">📅</span>
          <div>
            <div className="db-stat-number">0</div>
            <div className="db-stat-label">Upcoming Appointments</div>
          </div>
        </div>
        <div className="db-stat-card">
          <span className="db-stat-icon">💅</span>
          <div>
            <div className="db-stat-number">0</div>
            <div className="db-stat-label">Services Booked</div>
          </div>
        </div>
        <div className="db-stat-card">
          <span className="db-stat-icon">⭐</span>
          <div>
            <div className="db-stat-number">0</div>
            <div className="db-stat-label">Reviews Given</div>
          </div>
        </div>
      </div>
      <div className="db-quick-actions">
        <h3 className="db-section-heading">Quick Actions</h3>
        <div className="db-action-cards">
          <div className="db-action-card">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
            <h4>Book Appointment</h4>
            <p>Schedule a new beauty session</p>
          </div>
          <div className="db-action-card">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/>
            </svg>
            <h4>Browse Services</h4>
            <p>Explore available beauty services</p>
          </div>
          <div className="db-action-card">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
            </svg>
            <h4>Leave a Review</h4>
            <p>Share your beauty experience</p>
          </div>
        </div>
      </div>
    </>
  );
}

function Dashboard() {
  const navigate = useNavigate();
  const [active, setActive] = useState('overview');

  const handleLogout = () => {
    navigate('/login');
  };

  const activeLabel = navItems.find(n => n.key === active)?.label || 'Overview';

  return (
    <div className="dashboard-layout">
      {/* ── Sidebar ── */}
      <aside className="db-sidebar">
        <div className="db-sidebar-brand">
          <span className="db-brand-name">GlamSched</span>
        </div>
        <nav className="db-sidebar-nav">
          {navItems.map(item => (
            <button
              key={item.key}
              className={`db-nav-item${active === item.key ? ' active' : ''}`}
              onClick={() => setActive(item.key)}
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
          <PageContent active={active} />
        </div>
      </main>
    </div>
  );
}

export default Dashboard;

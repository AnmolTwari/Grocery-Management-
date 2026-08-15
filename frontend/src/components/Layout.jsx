import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { auth } from '../services/api'
import './AppShell.css'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/products', label: 'Products' },
  { to: '/inventory', label: 'Inventory' },
  { to: '/sales', label: 'Sales' },
  { to: '/reports', label: 'Reports' },
  { to: '/settings', label: 'Settings' },
]

export default function Layout() {
  const [menuOpen, setMenuOpen] = useState(false)

  function closeMenu() {
    setMenuOpen(false)
  }

  async function handleLogout() {
    await auth.logout()
    window.location.href = '/login'
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-top">
          <div className="brand">
            <img src="/logo.png" alt="ShopManager logo" className="brand-logo" />
          </div>
          <button
            type="button"
            className="menu-toggle"
            aria-label={menuOpen ? 'Close menu' : 'Open menu'}
            aria-expanded={menuOpen}
            aria-controls="mobile-nav"
            onClick={() => setMenuOpen((open) => !open)}
          >
            <span className="menu-bar" />
            <span className="menu-bar" />
            <span className="menu-bar" />
          </button>
        </div>

        <nav id="mobile-nav" aria-label="Main navigation" className={`mobile-nav${menuOpen ? ' open' : ''}`}>
          <ul className="nav-list">
            {NAV_ITEMS.map((item) => (
              <li key={item.label}>
                <NavLink
                  to={item.to}
                  end={item.end}
                  onClick={closeMenu}
                  className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
                >
                  {item.label}
                </NavLink>
              </li>
            ))}
          </ul>
          <div className="sidebar-footer">
            <button onClick={handleLogout} className="btn btn-secondary btn-sm w-full justify-start">
              ← Logout
            </button>
          </div>
        </nav>
      </aside>

      <main className="main">
        <Outlet />
      </main>
    </div>
  )
}
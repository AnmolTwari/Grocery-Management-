import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { auth } from '../services/api'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/products', label: 'Products' },
  { to: '/inventory', label: 'Inventory' },
  { to: '/sales', label: 'Sales' },
  { to: '/reports', label: 'Reports' },
  { to: '/settings', label: 'Settings' },
]

const NAV_LINK_BASE =
  'block rounded-sm px-3 py-3 text-[15px] font-medium text-secondary transition-colors hover:bg-bg hover:text-text focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary md:py-2'

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
    <div className="flex min-h-screen flex-col md:flex-row">
      <aside className="sticky top-0 z-10 flex w-full shrink-0 flex-col items-stretch gap-0 border-b border-border bg-surface px-3 py-2 shadow-sm md:static md:w-[232px] md:border-b-0 md:border-r md:gap-6 md:px-4 md:py-4 md:shadow-none">
        <div className="flex w-full items-center justify-between gap-2">
          <div className="flex items-center justify-center pb-2 md:pb-0">
            <img
              src="/logo.png"
              alt="ShopManager logo"
              className="h-11 w-auto max-w-full shrink-0 object-contain [filter:drop-shadow(0_2px_8px_rgba(15,23,42,0.18))] md:h-20"
            />
          </div>
          <button
            type="button"
            className="menu-toggle inline-flex h-10 w-10 cursor-pointer flex-col items-center justify-center gap-1 rounded-sm border-none bg-transparent hover:bg-bg md:hidden"
            aria-label={menuOpen ? 'Close menu' : 'Open menu'}
            aria-expanded={menuOpen}
            aria-controls="mobile-nav"
            onClick={() => setMenuOpen((open) => !open)}
          >
            <span className="menu-bar h-0.5 w-5 rounded-sm bg-text transition-[transform,opacity] duration-200" />
            <span className="menu-bar h-0.5 w-5 rounded-sm bg-text transition-[transform,opacity] duration-200" />
            <span className="menu-bar h-0.5 w-5 rounded-sm bg-text transition-[transform,opacity] duration-200" />
          </button>
        </div>

        <nav
          id="mobile-nav"
          aria-label="Main navigation"
          className={`${menuOpen ? 'flex' : 'hidden'} flex-col gap-1 pt-2 md:flex md:gap-0 md:pt-0`}
        >
          <ul className="m-0 flex list-none flex-col gap-1 p-0">
            {NAV_ITEMS.map((item) => (
              <li key={item.label}>
                <NavLink
                  to={item.to}
                  end={item.end}
                  onClick={closeMenu}
                  className={({ isActive }) =>
                    `${NAV_LINK_BASE}${isActive ? ' bg-primary-light font-semibold text-primary' : ''}`
                  }
                >
                  {item.label}
                </NavLink>
              </li>
            ))}
          </ul>
          <div className="mt-2 border-t border-border pt-2 md:mt-auto md:pt-4">
            <button
              onClick={handleLogout}
              className="inline-flex min-h-10 w-full cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold no-underline transition-colors hover:bg-bg disabled:opacity-60 md:min-h-0 md:justify-start"
            >
              ← Logout
            </button>
          </div>
        </nav>
      </aside>

      <main className="flex min-w-0 flex-1 flex-col">
        <Outlet />
      </main>
    </div>
  )
}

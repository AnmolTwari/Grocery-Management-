import './components/AppShell.css'

const NAV_ITEMS = [
  { label: 'Dashboard', href: '#' },
  { label: 'Products', href: '#' },
  { label: 'Inventory', href: '#' },
  { label: 'Sales', href: '#' },
  { label: 'Reports', href: '#' },
  { label: 'Settings', href: '#' },
]

function App() {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark" aria-hidden="true" />
          <span className="brand-name">Grocery Manager</span>
        </div>
        <nav aria-label="Main navigation">
          <ul className="nav-list">
            {NAV_ITEMS.map((item) => (
              <li key={item.label}>
                <a className="nav-link" href={item.href}>
                  {item.label}
                </a>
              </li>
            ))}
          </ul>
        </nav>
      </aside>

      <main className="main">
        <header className="topbar">
          <h1>Dashboard</h1>
        </header>

        <div className="content">
          <div className="card">
            <h2>Welcome to Grocery Manager</h2>
            <p>
              Phase 0 project setup is complete. The application shell is
              ready — products, inventory, and sales features are built in
              the next phases.
            </p>
          </div>
        </div>
      </main>
    </div>
  )
}

export default App
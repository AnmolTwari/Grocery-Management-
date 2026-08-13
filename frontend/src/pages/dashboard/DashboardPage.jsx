import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getDashboardSummary } from '../../services/dashboard'
import { formatCurrency, formatDateTime } from '../../utils/format'

const LOW_STOCK = 'Low Stock'
const OUT_OF_STOCK = 'Out of Stock'

function alarmClass(label) {
  if (label === OUT_OF_STOCK) return 'stat-alarm'
  if (label === LOW_STOCK) return 'stat-warn'
  return ''
}

export default function DashboardPage() {
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    getDashboardSummary()
      .then((data) => {
        if (!cancelled) setSummary(data)
      })
      .catch((err) => {
        if (!cancelled) setError(err.message)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  const tiles = summary
    ? [
        { label: 'Sales Today', value: String(summary.salesToday) },
        { label: 'Revenue Today', value: formatCurrency(summary.revenueToday) },
        { label: 'Profit Today', value: formatCurrency(summary.profitToday) },
        { label: 'Products', value: String(summary.totalProducts) },
        { label: OUT_OF_STOCK, value: String(summary.outOfStockCount) },
        { label: LOW_STOCK, value: String(summary.lowStockCount) },
      ]
    : []

  return (
    <div className="content">
      <div className="page-header">
        <h1 className="page-title">Dashboard</h1>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading && <p className="loading-text">Loading…</p>}

      {summary && (
        <>
          <div className="stats-grid">
            {tiles.map((tile) => (
              <div className={`card stat-card ${alarmClass(tile.label)}`} key={tile.label}>
                <div className="stat-value">{tile.value}</div>
                <div className="stat-label">{tile.label}</div>
              </div>
            ))}
          </div>

          <div className="quick-actions">
            <Link to="/sales/new" className="btn btn-primary">
              New Sale
            </Link>
            <Link to="/products/new" className="btn btn-secondary">
              Add Product
            </Link>
            <Link to="/inventory" className="btn btn-secondary">
              Stock In
            </Link>
          </div>

          <div className="card card-wide">
            <h2 className="section-title">Recent Sales</h2>
            {summary.recentSales.length === 0 ? (
              <p>No sales recorded yet.</p>
            ) : (
              <div className="table-wrap">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Sale</th>
                      <th>Items</th>
                      <th>Total</th>
                      <th>Time</th>
                    </tr>
                  </thead>
                  <tbody>
                    {summary.recentSales.map((sale) => (
                      <tr key={sale.id}>
                        <td>
                          <Link to={`/sales/${sale.id}`} className="link">
                            Sale #{sale.id}
                          </Link>
                        </td>
                        <td>{sale.itemCount}</td>
                        <td>{formatCurrency(sale.totalAmount)}</td>
                        <td>{formatDateTime(sale.createdAt)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  )
}
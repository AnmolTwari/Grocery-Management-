import { useEffect, useState } from 'react'
import { getReportSummary } from '../../services/reports'
import { formatCurrency } from '../../utils/format'

function toIso(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export default function ReportsPage() {
  const today = new Date()
  const [from, setFrom] = useState(toIso(new Date(today.getTime() - 29 * 86400000)))
  const [to, setTo] = useState(toIso(today))
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)
    getReportSummary(from, to)
      .then((data) => {
        if (!cancelled) setReport(data)
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
  }, [from, to])

  const tiles = report
    ? [
        { label: 'Orders', value: String(report.salesCount) },
        { label: 'Revenue', value: formatCurrency(report.totalAmount) },
        { label: 'Profit', value: formatCurrency(report.totalProfit) },
        { label: 'Avg Order Value', value: formatCurrency(report.averageOrderValue) },
      ]
    : []

  return (
    <div className="content">
      <div className="page-header">
        <h1 className="page-title">Reports</h1>
      </div>

      <div className="card report-range">
        <div className="field">
          <label className="label" htmlFor="from">
            From
          </label>
          <input
            id="from"
            type="date"
            className="input"
            value={from}
            max={to}
            onChange={(e) => setFrom(e.target.value)}
          />
        </div>
        <div className="field">
          <label className="label" htmlFor="to">
            To
          </label>
          <input
            id="to"
            type="date"
            className="input"
            value={to}
            min={from}
            max={toIso(new Date())}
            onChange={(e) => setTo(e.target.value)}
          />
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading && <p className="loading-text">Loading…</p>}

      {report && (
        <div className="stats-grid">
          {tiles.map((tile) => (
            <div className="card stat-card" key={tile.label}>
              <div className="stat-value">{tile.value}</div>
              <div className="stat-label">{tile.label}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
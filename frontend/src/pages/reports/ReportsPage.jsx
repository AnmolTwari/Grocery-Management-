import { useEffect, useState } from 'react'
import RefreshButton from '../../components/RefreshButton'
import { api } from '../../services/api'
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
  const [reload, setReload] = useState(0)

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
  }, [from, to, reload])

  function handleRefresh() {
    api.clearCache()
    setReload((n) => n + 1)
  }

  const tiles = report
    ? [
        { label: 'Orders', value: String(report.salesCount) },
        { label: 'Revenue', value: formatCurrency(report.totalAmount) },
        { label: 'Profit', value: formatCurrency(report.totalProfit) },
        { label: 'Avg Order Value', value: formatCurrency(report.averageOrderValue) },
      ]
    : []

  return (
    <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-lg min-[481px]:text-xl md:text-2xl">Reports</h1>
        <RefreshButton onClick={handleRefresh} disabled={loading} />
      </div>

      <div className="flex max-w-none flex-col gap-3 rounded-lg border border-border bg-surface p-4 shadow-sm md:max-w-[480px] md:flex-row md:gap-4 md:p-6">
        <div className="flex flex-1 flex-col gap-1">
          <label className="text-sm font-semibold" htmlFor="from">
            From
          </label>
          <input
            id="from"
            type="date"
            className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
            value={from}
            max={to}
            onChange={(e) => setFrom(e.target.value)}
          />
        </div>
        <div className="flex flex-1 flex-col gap-1">
          <label className="text-sm font-semibold" htmlFor="to">
            To
          </label>
          <input
            id="to"
            type="date"
            className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
            value={to}
            min={from}
            max={toIso(new Date())}
            onChange={(e) => setTo(e.target.value)}
          />
        </div>
      </div>

      {error && (
        <div className="rounded-sm border border-[#fecaca] bg-[#fee2e2] px-4 py-3 text-sm text-[#991b1b]">
          {error}
        </div>
      )}

      {loading && <p className="text-secondary">Loading…</p>}

      {report && (
        <div className="grid grid-cols-2 gap-3 min-[481px]:grid-cols-[repeat(auto-fit,minmax(170px,1fr))] min-[481px]:gap-4">
          {tiles.map((tile) => (
            <div
              className="rounded-lg border border-border bg-surface p-3 shadow-sm min-[481px]:p-4"
              key={tile.label}
            >
              <div className="text-[19px] leading-tight font-bold text-text min-[481px]:text-[22px] md:text-[26px]">
                {tile.value}
              </div>
              <div className="mt-2 text-[13px] text-secondary">{tile.label}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
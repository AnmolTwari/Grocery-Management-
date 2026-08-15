import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getDashboardSummary } from '../../services/dashboard'
import { formatCurrency, formatDateTime } from '../../utils/format'

const LOW_STOCK = 'Low Stock'
const OUT_OF_STOCK = 'Out of Stock'

function alarmClass(label) {
  if (label === OUT_OF_STOCK) return 'text-[#991b1b]'
  if (label === LOW_STOCK) return 'text-[#92400e]'
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
    <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-lg min-[481px]:text-xl md:text-2xl">Dashboard</h1>
      </div>

      {error && (
        <div className="rounded-sm border border-[#fecaca] bg-[#fee2e2] px-4 py-3 text-sm text-[#991b1b]">
          {error}
        </div>
      )}

      {loading && <p className="text-secondary">Loading…</p>}

      {summary && (
        <>
          <div className="grid grid-cols-2 gap-3 min-[481px]:grid-cols-[repeat(auto-fit,minmax(170px,1fr))] min-[481px]:gap-4">
            {tiles.map((tile) => (
              <div
                className="rounded-lg border border-border bg-surface p-3 shadow-sm min-[481px]:p-4"
                key={tile.label}
              >
                <div
                  className={`text-[19px] leading-tight font-bold text-text min-[481px]:text-[22px] md:text-[26px] ${alarmClass(tile.label)}`}
                >
                  {tile.value}
                </div>
                <div className="mt-2 text-[13px] text-secondary">{tile.label}</div>
              </div>
            ))}
          </div>

          <div className="flex flex-wrap gap-2">
            <Link
              to="/sales/new"
              className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-transparent bg-primary px-4 py-2 text-sm font-semibold text-white transition-colors hover:enabled:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
            >
              New Sale
            </Link>
            <Link
              to="/products/new"
              className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-4 py-2 text-sm font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
            >
              Add Product
            </Link>
            <Link
              to="/inventory"
              className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-4 py-2 text-sm font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
            >
              Stock In
            </Link>
          </div>

          <div className="rounded-lg border border-border bg-surface p-4 shadow-sm md:p-6">
            <h2 className="mb-4 text-base font-semibold">Recent Sales</h2>
            {summary.recentSales.length === 0 ? (
              <p>No sales recorded yet.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full min-w-[600px] border-collapse [&_tbody_tr:last-child_td]:border-b-0 [&_tbody_tr:hover]:bg-bg md:min-w-0">
                  <thead>
                    <tr>
                      <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                        Sale
                      </th>
                      <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                        Items
                      </th>
                      <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                        Total
                      </th>
                      <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                        Time
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {summary.recentSales.map((sale) => (
                      <tr key={sale.id}>
                        <td className="border-b border-border p-3 text-left align-middle">
                          <Link to={`/sales/${sale.id}`} className="font-semibold text-primary hover:underline">
                            Sale #{sale.id}
                          </Link>
                        </td>
                        <td className="border-b border-border p-3 text-left align-middle">
                          {sale.itemCount}
                        </td>
                        <td className="border-b border-border p-3 text-left align-middle">
                          {formatCurrency(sale.totalAmount)}
                        </td>
                        <td className="border-b border-border p-3 text-left align-middle">
                          {formatDateTime(sale.createdAt)}
                        </td>
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
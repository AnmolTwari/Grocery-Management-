import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  IconAlert,
  IconBanknote,
  IconBox,
  IconCart,
  IconChart,
  IconRefresh,
  IconTrendDown,
  IconTrendUp,
} from '../../components/icons'
import { getDashboardSummary } from '../../services/dashboard'
import { formatCurrency, formatDateTime } from '../../utils/format'
import { api } from '../../services/api'

function greeting() {
  const hour = new Date().getHours()
  if (hour < 12) return 'Good morning'
  if (hour < 17) return 'Good afternoon'
  return 'Good evening'
}

function TrendPill({ today, yesterday, format }) {
  const same = yesterday === null || yesterday === undefined
  const diff = same ? 0 : today - yesterday
  const pct = same || yesterday === 0 ? null : Math.abs((diff / yesterday) * 100)
  if (same || diff === 0) {
    return <span className="text-xs text-muted">Same as yesterday</span>
  }
  const up = diff > 0
  const text = format ? `${format(Math.abs(diff))} (${pct.toFixed(0)}%)` : `${Math.abs(diff)} (${pct.toFixed(0)}%)`
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-semibold ${
        up ? 'bg-primary-light text-[#166534]' : 'bg-[#fee2e2] text-[#991b1b]'
      }`}
    >
      {up ? <IconTrendUp size={12} /> : <IconTrendDown size={12} />}
      {text} vs yesterday
    </span>
  )
}

export default function DashboardPage() {
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [reload, setReload] = useState(0)

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
  }, [reload])

  function handleRefresh() {
    api.clearCache()
    setReload((n) => n + 1)
  }

  const cards = summary
    ? [
        {
          label: 'Sales Today',
          value: String(summary.salesToday),
          hint: 'Sales',
          Icon: IconCart,
          accent: 'bg-primary-light text-primary',
          trend: <TrendPill today={summary.salesToday} yesterday={summary.salesYesterday} />,
        },
        {
          label: 'Revenue Today',
          value: formatCurrency(summary.revenueToday),
          hint: 'Revenue',
          Icon: IconBanknote,
          accent: 'bg-info-light text-info',
          trend: (
            <TrendPill
              today={Number(summary.revenueToday)}
              yesterday={Number(summary.revenueYesterday)}
              format={(n) => formatCurrency(n)}
            />
          ),
        },
        {
          label: 'Profit Today',
          value: formatCurrency(summary.profitToday),
          hint: 'Profit',
          Icon: IconTrendUp,
          accent: 'bg-primary-light text-primary',
          trend: <span className="text-xs text-muted">Estimated margin</span>,
        },
        {
          label: 'Products',
          value: String(summary.totalProducts),
          hint: 'Products',
          Icon: IconBox,
          accent: 'bg-info-light text-info',
          trend: (
            <span className={`text-xs ${summary.lowStockCount > 0 || summary.outOfStockCount > 0 ? 'text-warning' : 'text-muted'}`}>
              {summary.lowStockCount} low · {summary.outOfStockCount} out of stock
            </span>
          ),
        },
      ]
    : []

  const revenueSeries = summary?.dailyRevenue ?? []
  const maxRevenue = Math.max(1, ...revenueSeries.map((p) => Number(p.total)))
  const todayLabel = new Date().toDateString()

  return (
    <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h1 className="text-lg font-semibold min-[481px]:text-xl md:text-2xl">
            {greeting()}, {summary ? 'welcome back' : 'loading…'}
          </h1>
          <p className="mt-1 text-sm text-secondary">
            Here&apos;s what&apos;s happening in your shop today.
          </p>
        </div>
        <button
          type="button"
          onClick={handleRefresh}
          disabled={loading}
          title="Refresh dashboard"
          aria-label="Refresh dashboard"
          className="mt-0.5 flex h-9 w-9 shrink-0 cursor-pointer items-center justify-center rounded-sm border border-border bg-surface text-secondary transition-colors hover:enabled:border-primary hover:enabled:text-primary disabled:cursor-not-allowed disabled:opacity-60"
        >
          <IconRefresh className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {error && (
        <div className="rounded-sm border border-[#fecaca] bg-[#fee2e2] px-4 py-3 text-sm text-[#991b1b]">
          {error}
        </div>
      )}

      {loading && <p className="text-secondary">Loading…</p>}

      {summary && (
        <>
          {(summary.lowStockCount > 0 || summary.outOfStockCount > 0) && (
            <Link
              to="/inventory"
              className="flex flex-col items-start gap-1.5 rounded-sm border border-[#fde68a] bg-[#fef9c3] px-4 py-3 text-sm text-[#713f12] transition-colors hover:bg-[#fef3c7] sm:flex-row sm:items-center sm:gap-3"
            >
              <span className="flex items-center gap-3">
                <IconAlert size={18} className="shrink-0 text-warning" />
                <span>
                  <strong>
                    {summary.outOfStockCount > 0
                      ? `${summary.outOfStockCount} product${summary.outOfStockCount > 1 ? 's are' : ' is'} out of stock`
                      : ''}
                    {summary.outOfStockCount > 0 && summary.lowStockCount > 0 ? ' and ' : ''}
                    {summary.lowStockCount > 0
                      ? `${summary.lowStockCount} product${summary.lowStockCount > 1 ? 's are' : ' is'} running low`
                      : ''}
                  </strong>{' '}
                  — restock them now to avoid missed sales.
                </span>
              </span>
              <span className="font-semibold sm:ml-auto sm:shrink-0">View inventory →</span>
            </Link>
          )}

          <div className="grid grid-cols-2 gap-3 min-[481px]:gap-4 lg:grid-cols-4">
            {cards.map((card) => (
              <div
                className="flex min-w-0 flex-col gap-2.5 rounded-lg border border-border bg-surface p-3 shadow-sm md:gap-3 md:p-4"
                key={card.label}
              >
                <div className="flex items-center justify-between gap-2">
                  <span className="min-w-0 text-[12px] font-medium text-secondary min-[481px]:text-[13px]">
                    {card.label}
                  </span>
                  <span
                    className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full min-[481px]:h-9 min-[481px]:w-9 ${card.accent}`}
                  >
                    <card.Icon size={18} />
                  </span>
                </div>
                <div className="min-w-0 text-[19px] leading-tight font-bold text-text [overflow-wrap:anywhere] min-[481px]:text-[22px] md:text-[26px]">
                  {card.value}
                </div>
                <div className="max-w-full text-xs text-secondary">{card.trend}</div>
              </div>
            ))}
          </div>

          <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
            <div className="flex flex-col gap-3 rounded-lg border border-border bg-surface p-4 shadow-sm lg:col-span-2 md:p-6">
              <div className="flex items-center justify-between gap-2">
                <h2 className="flex items-center gap-2 text-base font-semibold">
                  <IconChart size={18} className="text-primary" />
                  Last 7 days
                </h2>
                <span className="text-xs text-secondary">Revenue</span>
              </div>
              <div className="flex h-40 items-end gap-2 md:h-48 md:gap-3">
                {revenueSeries.map((point) => {
                  const height = Math.max(4, (Number(point.total) / maxRevenue) * 100)
                  const isToday = new Date(point.date).toDateString() === todayLabel
                  return (
                    <div
                      className="group flex flex-1 flex-col items-center gap-1.5"
                      key={point.date}
                      title={`${point.date} · ${formatCurrency(point.total)}`}
                    >
                      <div
                        className={`w-full rounded-t-sm transition-colors ${
                          isToday ? 'bg-primary' : 'bg-primary-light group-hover:bg-primary/70'
                        }`}
                        style={{ height: `${height}%` }}
                      />
                      <span className={`text-[10px] md:text-xs ${isToday ? 'font-semibold text-primary' : 'text-muted'}`}>
                        {isToday
                          ? 'Today'
                          : new Date(point.date).toLocaleDateString(undefined, { weekday: 'short' })}
                      </span>
                    </div>
                  )
                })}
              </div>
              <div className="flex items-center justify-between gap-2 border-t border-border pt-3 text-sm">
                <span className="text-secondary">Total this week</span>
                <span className="font-semibold">
                  {formatCurrency(revenueSeries.reduce((sum, p) => sum + Number(p.total), 0))}
                </span>
              </div>
            </div>

            <div className="flex flex-col gap-3 rounded-lg border border-border bg-surface p-4 shadow-sm md:p-6">
              <h2 className="text-base font-semibold">Quick actions</h2>
              <div className="flex flex-1 flex-col gap-2">
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
            </div>
          </div>

          <div className="rounded-lg border border-border bg-surface p-4 shadow-sm md:p-6">
            <h2 className="mb-4 text-base font-semibold">Recent Sales</h2>
            {summary.recentSales.length === 0 ? (
              <p className="text-secondary">No sales recorded yet.</p>
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
                          <Link
                            to={`/sales/${sale.id}`}
                            className="font-semibold text-primary hover:underline"
                          >
                            Sale #{sale.id}
                          </Link>
                        </td>
                        <td className="border-b border-border p-3 text-left align-middle">
                          {sale.itemCount}
                        </td>
                        <td className="border-b border-border p-3 text-left align-middle font-semibold">
                          {formatCurrency(sale.totalAmount)}
                        </td>
                        <td className="border-b border-border p-3 text-left align-middle text-secondary">
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
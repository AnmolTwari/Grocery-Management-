import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import RefreshButton from '../../components/RefreshButton'
import { api } from '../../services/api'
import { listSales } from '../../services/sales'
import { formatCurrency, formatDateTime } from '../../utils/format'

const PAGE_SIZE = 20

export default function SalesPage() {
  const [sales, setSales] = useState(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [reload, setReload] = useState(0)

  useEffect(() => {
    let cancelled = false
    listSales({ page, size: PAGE_SIZE })
      .then((data) => {
        if (cancelled) return
        if (data.empty && page > 0) {
          setPage(page - 1)
          return
        }
        setSales(data)
        setError(null)
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
  }, [page, reload])

  function handleRefresh() {
    api.clearCache()
    setReload((n) => n + 1)
  }

  const isEmpty = sales && sales.content.length === 0

  return (
    <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-lg font-semibold min-[481px]:text-xl md:text-2xl">Sales</h1>
        <div className="flex shrink-0 items-center gap-2">
          <RefreshButton onClick={handleRefresh} disabled={loading} />
          <Link
            to="/sales/new"
            className="inline-flex min-h-10 shrink-0 cursor-pointer items-center justify-center gap-2 rounded-sm border border-transparent bg-primary px-4 py-2 text-sm font-semibold text-white transition-colors hover:enabled:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
          >
            ＋ New Sale
          </Link>
        </div>
      </div>

      {error && (
        <div className="rounded-sm border border-[#fecaca] bg-[#fee2e2] px-4 py-3 text-sm text-[#991b1b]">
          {error}
        </div>
      )}

      {isEmpty && !loading && (
        <div className="rounded-lg border border-dashed border-border bg-surface p-10 text-center text-secondary shadow-sm md:p-14">
          <p>No sales yet. Start a new sale to record your first one.</p>
        </div>
      )}

      {sales && !isEmpty && (
        <>
          <div className="overflow-x-auto rounded-lg border border-border bg-surface shadow-sm">
            <table className="w-full min-w-[600px] border-collapse [&_thead_tr]:bg-bg [&_tbody_tr:last-child_td]:border-b-0 [&_tbody_tr:hover]:bg-bg md:min-w-0">
              <thead>
                <tr>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Sale #
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Date &amp; Time
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Items
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Total
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase"></th>
                </tr>
              </thead>
              <tbody>
                {sales.content.map((sale) => (
                  <tr key={sale.id}>
                    <td className="border-b border-border p-3 text-left align-middle font-semibold">
                      #{sale.id}
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      {formatDateTime(sale.createdAt)}
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      {sale.itemCount}
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      {formatCurrency(sale.totalAmount)}
                    </td>
                    <td className="flex items-center gap-2 border-b border-border p-3 text-left align-middle whitespace-nowrap">
                      <Link
                        to={`/sales/${sale.id}`}
                        className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                      >
                        View
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {sales.totalPages > 1 && (
            <div className="flex items-center justify-center gap-3">
              <button
                type="button"
                className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                disabled={sales.first}
                onClick={() => setPage(page - 1)}
              >
                Prev
              </button>
              <span className="text-sm text-secondary">
                Page {sales.number + 1} of {sales.totalPages}
              </span>
              <button
                type="button"
                className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                disabled={sales.last}
                onClick={() => setPage(page + 1)}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}

      {loading && <p className="text-secondary">Loading…</p>}
    </div>
  )
}
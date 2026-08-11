import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listSales } from '../../services/sales'
import { formatCurrency, formatDateTime } from '../../utils/format'

const PAGE_SIZE = 20

export default function SalesPage() {
  const [sales, setSales] = useState(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

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
  }, [page])

  const isEmpty = sales && sales.content.length === 0

  return (
    <div className="content">
      <div className="page-header">
        <h1 className="page-title">Sales</h1>
        <Link to="/sales/new" className="btn btn-primary">
          ＋ New Sale
        </Link>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {isEmpty && !loading && (
        <div className="card empty-state">
          <p>No sales yet. Start a new sale to record your first one.</p>
        </div>
      )}

      {sales && !isEmpty && (
        <>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Sale #</th>
                  <th>Date &amp; Time</th>
                  <th>Items</th>
                  <th>Total</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {sales.content.map((sale) => (
                  <tr key={sale.id}>
                    <td className="product-name">#{sale.id}</td>
                    <td>{formatDateTime(sale.createdAt)}</td>
                    <td>{sale.itemCount}</td>
                    <td>{formatCurrency(sale.totalAmount)}</td>
                    <td className="actions">
                      <Link to={`/sales/${sale.id}`} className="btn btn-secondary btn-sm">
                        View
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {sales.totalPages > 1 && (
            <div className="pagination">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                disabled={sales.first}
                onClick={() => setPage(page - 1)}
              >
                Prev
              </button>
              <span className="pagination-info">
                Page {sales.number + 1} of {sales.totalPages}
              </span>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                disabled={sales.last}
                onClick={() => setPage(page + 1)}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}

      {loading && <p className="loading-text">Loading…</p>}
    </div>
  )
}
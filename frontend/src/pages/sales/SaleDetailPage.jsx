import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { getSale } from '../../services/sales'
import { formatCurrency, formatDateTime, toNumber } from '../../utils/format'
import { UNIT_LABELS } from '../../utils/units'

export default function SaleDetailPage() {
  const { id } = useParams()
  const location = useLocation()
  const justCompleted = Boolean(location.state?.justCompleted)

  const [sale, setSale] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    getSale(id)
      .then((data) => {
        if (!cancelled) setSale(data)
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
  }, [id])

  if (loading) {
    return (
      <div className="content">
        <p className="loading-text">Loading…</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="content">
        <div className="alert alert-danger">{error}</div>
      </div>
    )
  }

  const profit = sale.items.reduce(
    (sum, item) =>
      sum + (Number(item.unitPrice) - Number(item.purchasePrice)) * toNumber(item.quantity),
    0,
  )

  return (
    <div className="content">
      <div className="page-header">
        <h1 className="page-title">Sale #{sale.id}</h1>
        <Link to="/sales" className="btn btn-secondary btn-sm">
          ← Back to Sales
        </Link>
      </div>

      {justCompleted && (
        <div className="alert alert-success">
          Sale #{sale.id} completed — stock was reduced automatically.
        </div>
      )}

      <p className="subtitle">{formatDateTime(sale.createdAt)}</p>

      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Product</th>
              <th>Qty</th>
              <th>Unit Price</th>
              <th>Line Total</th>
            </tr>
          </thead>
          <tbody>
            {sale.items.map((item, index) => (
              <tr key={`${item.productId}-${index}`}>
                <td>
                  <div className="product-name">{item.productName}</div>
                  {item.productSku && <div className="product-brand">{item.productSku}</div>}
                </td>
                <td>
                  {item.quantity} {UNIT_LABELS[item.unit] ?? item.unit}
                </td>
                <td>{formatCurrency(item.unitPrice)}</td>
                <td>{formatCurrency(item.lineTotal)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="card total-box mt-4">
        <div>
          <div className="pagination-info">Estimated gross profit</div>
          <span className="total-amount">{formatCurrency(profit)}</span>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div className="pagination-info">Sale total</div>
          <span className="total-amount">{formatCurrency(sale.totalAmount)}</span>
        </div>
      </div>
    </div>
  )
}
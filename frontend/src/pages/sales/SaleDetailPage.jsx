import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { IconArrowLeft } from '../../components/icons'
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
      <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
        <p className="text-secondary">Loading…</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
        <div className="rounded-sm border border-[#fecaca] bg-[#fee2e2] px-4 py-3 text-sm text-[#991b1b]">
          {error}
        </div>
      </div>
    )
  }

  const profit = sale.items.reduce(
    (sum, item) =>
      sum + (Number(item.unitPrice) - Number(item.purchasePrice)) * toNumber(item.quantity),
    0,
  )

  return (
    <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
      <div className="flex flex-col gap-1">
        <Link
          to="/sales"
          className="inline-flex w-fit items-center gap-1.5 text-sm font-semibold text-primary hover:underline"
        >
          <IconArrowLeft size={16} />
          Back to Sales
        </Link>
        <h1 className="text-lg min-[481px]:text-xl md:text-2xl">Sale #{sale.id}</h1>
      </div>

      {justCompleted && (
        <div className="rounded-sm border border-[#bbf7d0] bg-primary-light px-4 py-3 text-sm text-[#166534]">
          Sale #{sale.id} completed — stock was reduced automatically.
        </div>
      )}

      <p className="m-0 text-sm text-secondary">{formatDateTime(sale.createdAt)}</p>

      <div className="overflow-x-auto rounded-lg border border-border bg-surface shadow-sm">
        <table className="w-full min-w-[600px] border-collapse [&_tbody_tr:last-child_td]:border-b-0 [&_tbody_tr:hover]:bg-bg md:min-w-0">
          <thead>
            <tr>
              <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                Product
              </th>
              <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                Qty
              </th>
              <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                Unit Price
              </th>
              <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                Line Total
              </th>
            </tr>
          </thead>
          <tbody>
            {sale.items.map((item, index) => (
              <tr key={`${item.productId}-${index}`}>
                <td className="border-b border-border p-3 text-left align-middle">
                  <div className="font-semibold">{item.productName}</div>
                  {item.productSku && <div className="text-xs text-secondary">{item.productSku}</div>}
                </td>
                <td className="border-b border-border p-3 text-left align-middle">
                  {item.quantity} {UNIT_LABELS[item.unit] ?? item.unit}
                </td>
                <td className="border-b border-border p-3 text-left align-middle">
                  {formatCurrency(item.unitPrice)}
                </td>
                <td className="border-b border-border p-3 text-left align-middle">
                  {formatCurrency(item.lineTotal)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="flex flex-col items-start gap-2 rounded-lg border border-border bg-surface p-4 pt-4 shadow-sm md:flex-row md:items-center md:justify-between md:gap-4 md:p-6">
        <div>
          <div className="text-sm text-secondary">Estimated gross profit</div>
          <span className="text-[22px] font-bold">{formatCurrency(profit)}</span>
        </div>
        <div className="text-right">
          <div className="text-sm text-secondary">Sale total</div>
          <span className="text-[22px] font-bold">{formatCurrency(sale.totalAmount)}</span>
        </div>
      </div>
    </div>
  )
}
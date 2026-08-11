import { STOCK_STATUS_LABELS } from '../utils/units'

export default function StockStatusBadge({ status }) {
  const className = `badge badge-${String(status).toLowerCase()}`
  return <span className={className}>{STOCK_STATUS_LABELS[status] ?? status}</span>
}
import { STOCK_STATUS_LABELS } from '../utils/units'

const STATUS_CLASSES = {
  IN_STOCK: 'bg-primary-light text-[#166534]',
  LOW_STOCK: 'bg-[#fef3c7] text-[#92400e]',
  OUT_OF_STOCK: 'bg-[#fee2e2] text-[#991b1b]',
}

export default function StockStatusBadge({ status }) {
  return (
    <span
      className={`inline-block rounded-full px-2 py-0.5 text-xs font-semibold whitespace-nowrap ${STATUS_CLASSES[status] ?? ''}`}
    >
      {STOCK_STATUS_LABELS[status] ?? status}
    </span>
  )
}

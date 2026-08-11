const currencyFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
})

export function formatCurrency(value) {
  if (value === null || value === undefined || value === '') return '—'
  return currencyFormatter.format(Number(value))
}

/** Converts a form input to a number, treating empty/invalid input as 0. */
export function toNumber(value) {
  if (value === null || value === undefined || value === '') return 0
  const parsed = Number.parseFloat(value)
  return Number.isNaN(parsed) ? 0 : parsed
}

/** Formats a date/time value (e.g. "2026-08-10T23:07:26") for display. */
export function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return date.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: true,
  })
}
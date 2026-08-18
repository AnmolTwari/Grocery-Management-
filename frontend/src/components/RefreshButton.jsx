import { IconRefresh } from './icons'

export default function RefreshButton({ onClick, disabled = false, label = 'Refresh' }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-4 py-2 text-sm font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
    >
      <IconRefresh className="h-4 w-4" />
      {label}
    </button>
  )
}
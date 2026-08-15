import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import StockStatusBadge from '../../components/StockStatusBadge'
import { createSale } from '../../services/sales'
import { listProducts } from '../../services/products'
import { formatCurrency, toNumber } from '../../utils/format'
import { UNIT_LABELS } from '../../utils/units'

export default function NewSalePage() {
  const navigate = useNavigate()

  const [search, setSearch] = useState('')
  const [results, setResults] = useState([])
  const [searching, setSearching] = useState(false)

  const [lines, setLines] = useState([])
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)

  useEffect(() => {
    let cancelled = false
    const timer = setTimeout(() => {
      const term = search.trim()
      if (!term) {
        setResults([])
        return
      }
      setSearching(true)
      listProducts({ search: term, page: 0, size: 8 })
        .then((data) => {
          if (!cancelled) setResults(data.content)
        })
        .catch((err) => {
          if (!cancelled) setError(err.message)
        })
        .finally(() => {
          if (!cancelled) setSearching(false)
        })
    }, 250)
    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [search])

  function addProduct(product) {
    setError(null)
    setSuccess(null)
    setLines((current) => {
      const existing = current.find((line) => line.productId === product.id)
      if (existing) {
        return current.map((line) =>
          line.productId === product.id
            ? { ...line, quantity: String(toNumber(line.quantity) + 1) }
            : line,
        )
      }
      return [
        ...current,
        {
          productId: product.id,
          name: product.name,
          sku: product.sku,
          unit: product.unit,
          unitPrice: product.sellingPrice,
          currentQuantity: product.currentQuantity,
          quantity: '1',
        },
      ]
    })
    setSearch('')
  }

  function setQuantity(index, value) {
    setLines((current) =>
      current.map((line, i) => (i === index ? { ...line, quantity: value } : line)),
    )
  }

  function removeLine(index) {
    setLines((current) => current.filter((_, i) => i !== index))
  }

  function lineTotal(line) {
    return toNumber(line.quantity) * Number(line.unitPrice)
  }

  const bagTotal = lines.reduce((sum, line) => sum + lineTotal(line), 0)

  async function completeSale() {
    setError(null)
    setSuccess(null)
    if (lines.length === 0) {
      setError('Add at least one product to the sale.')
      return
    }
    for (const line of lines) {
      if (toNumber(line.quantity) <= 0) {
        setError(`Quantity for "${line.name}" must be greater than zero.`)
        return
      }
    }

    const payload = {
      items: lines.map((line) => ({
        productId: line.productId,
        quantity: toNumber(line.quantity),
      })),
    }

    setSubmitting(true)
    try {
      const sale = await createSale(payload)
      setSuccess(`Sale #${sale.id} completed.`)
      navigate(`/sales/${sale.id}`, { state: { justCompleted: true } })
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-lg min-[481px]:text-xl md:text-2xl">New Sale</h1>
        <Link
          to="/sales"
          className="inline-flex min-h-10 shrink-0 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
        >
          ← Back to Sales
        </Link>
      </div>

      {error && (
        <div className="rounded-sm border border-[#fecaca] bg-[#fee2e2] px-4 py-3 text-sm text-[#991b1b]">
          {error}
        </div>
      )}
      {success && !error && (
        <div className="rounded-sm border border-[#bbf7d0] bg-primary-light px-4 py-3 text-sm text-[#166534]">
          {success}
        </div>
      )}

      <div className="rounded-lg border border-border bg-surface p-4 shadow-sm md:p-6">
        <h2 className="mb-4 text-base font-semibold">Add items</h2>
        <div className="flex flex-col gap-2 min-[481px]:flex-row min-[481px]:flex-wrap">
          <input
            type="search"
            className="min-h-10 w-full rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary min-[481px]:min-w-40 min-[481px]:flex-1"
            placeholder="Search products by name or SKU…"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        {searching && <p className="mt-2 text-secondary">Searching…</p>}
        {!searching && search.trim() && results.length === 0 && (
          <p className="mt-2 text-secondary">No products match “{search.trim()}”.</p>
        )}
        {results.length > 0 && (
          <ul className="m-0 mt-2 list-none overflow-hidden rounded-sm border border-border p-0">
            {results.map((product) => {
              const outOfStock = Number(product.currentQuantity) <= 0
              return (
                <li
                  className="flex items-start justify-between gap-3 border-b border-border bg-surface px-3 py-2 last:border-b-0 md:items-center"
                  key={product.id}
                >
                  <div>
                    <div className="font-semibold">{product.name}</div>
                    <div className="text-xs text-secondary">
                      {product.brand ? `${product.brand} · ` : ''}
                      {product.currentQuantity} {UNIT_LABELS[product.unit] ?? product.unit} ·{' '}
                      {formatCurrency(product.sellingPrice)}
                    </div>
                  </div>
                  <div className="flex items-center gap-2 whitespace-nowrap">
                    <StockStatusBadge status={product.stockStatus} />
                    <button
                      type="button"
                      className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-transparent bg-primary px-3 py-1 text-[13px] font-semibold text-white transition-colors hover:enabled:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                      disabled={outOfStock}
                      onClick={() => addProduct(product)}
                    >
                      {outOfStock ? 'Out of stock' : 'Add'}
                    </button>
                  </div>
                </li>
              )
            })}
          </ul>
        )}
      </div>

      <div className="rounded-lg border border-border bg-surface p-4 shadow-sm md:p-6">
        <h2 className="mb-4 text-base font-semibold">Sale items</h2>

        {lines.length === 0 ? (
          <p className="p-6 text-center text-secondary">
            No items yet. Search and add products above.
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[600px] border-collapse [&_tbody_tr:last-child_td]:border-b-0 [&_tbody_tr:hover]:bg-bg md:min-w-0">
              <thead>
                <tr>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Product
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Unit Price
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Qty
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Line Total
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase"></th>
                </tr>
              </thead>
              <tbody>
                {lines.map((line, index) => (
                  <tr key={line.productId}>
                    <td className="border-b border-border p-3 text-left align-middle">
                      <div className="font-semibold">{line.name}</div>
                      {line.sku && <div className="text-xs text-secondary">{line.sku}</div>}
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      {formatCurrency(line.unitPrice)}
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      <input
                        className="min-h-10 w-[90px] rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
                        type="number"
                        min="0.001"
                        step="any"
                        value={line.quantity}
                        onChange={(event) => setQuantity(index, event.target.value)}
                      />
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      {formatCurrency(lineTotal(line))}
                    </td>
                    <td className="flex items-center gap-2 border-b border-border p-3 text-left align-middle whitespace-nowrap">
                      <button
                        type="button"
                        className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-danger transition-colors hover:enabled:border-danger hover:enabled:bg-[#fef2f2] disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                        onClick={() => removeLine(index)}
                      >
                        Remove
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="mt-4 flex flex-col items-start gap-2 border-t border-border pt-4 md:flex-row md:items-center md:justify-between md:gap-4">
          <span className="text-sm font-semibold">Total</span>
          <span className="text-[22px] font-bold">{formatCurrency(bagTotal)}</span>
        </div>

        <div className="mt-6 flex flex-col-reverse gap-2 md:flex-row md:justify-end">
          <button
            type="button"
            className="inline-flex min-h-10 w-full cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-4 py-2 text-sm font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0 md:w-auto"
            disabled={lines.length === 0}
            onClick={() => setLines([])}
          >
            Clear
          </button>
          <button
            type="button"
            className="inline-flex min-h-10 w-full cursor-pointer items-center justify-center gap-2 rounded-sm border border-transparent bg-primary px-4 py-2 text-sm font-semibold text-white transition-colors hover:enabled:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0 md:w-auto"
            disabled={submitting || lines.length === 0}
            onClick={completeSale}
          >
            {submitting ? 'Completing…' : 'Complete Sale'}
          </button>
        </div>
      </div>
    </div>
  )
}
import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { IconArrowLeft } from '../../components/icons'
import StockStatusBadge from '../../components/StockStatusBadge'
import { createSale } from '../../services/sales'
import { listPopularProducts, listProducts } from '../../services/products'
import { formatCurrency, toNumber } from '../../utils/format'
import { UNIT_LABELS } from '../../utils/units'

const COUNT_UNITS = new Set(['PIECE', 'PACKET', 'BOX', 'BOTTLE'])

export default function NewSalePage() {
  const navigate = useNavigate()

  const [search, setSearch] = useState('')
  const [results, setResults] = useState([])
  const [searching, setSearching] = useState(false)
  const [selected, setSelected] = useState([])
  const [dropdownOpen, setDropdownOpen] = useState(false)
  const [popular, setPopular] = useState(null)

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

  useEffect(() => {
    let cancelled = false
    listPopularProducts()
      .then((data) => {
        if (!cancelled) setPopular(data)
      })
      .catch(() => {
        if (!cancelled) setPopular([])
      })
    return () => {
      cancelled = true
    }
  }, [])

  function addProducts(products) {
    setError(null)
    setSuccess(null)
    setLines((current) => {
      const next = [...current]
      for (const product of products) {
        const index = next.findIndex((line) => line.productId === product.id)
        if (index !== -1) {
          next[index] = {
            ...next[index],
            quantity: String(toNumber(next[index].quantity) + 1),
          }
        } else {
          next.push({
            productId: product.id,
            name: product.name,
            sku: product.sku,
            unit: product.unit,
            unitPrice: product.sellingPrice,
            currentQuantity: product.currentQuantity,
            quantity: '1',
          })
        }
      }
      return next
    })
  }

  function addProduct(product) {
    addProducts([product])
  }

  const visibleProducts = search.trim() ? results : (popular ?? [])

  function toggleSelect(productId) {
    setSelected((current) =>
      current.includes(productId)
        ? current.filter((id) => id !== productId)
        : [...current, productId],
    )
  }

  function toggleSelectAll() {
    const selectable = visibleProducts
      .filter((product) => Number(product.currentQuantity) > 0)
      .map((product) => product.id)
    setSelected((current) => {
      const allSelected = selectable.length > 0 && selectable.every((id) => current.includes(id))
      return allSelected
        ? current.filter((id) => !selectable.includes(id))
        : [...new Set([...current, ...selectable])]
    })
  }

  function addSelected() {
    const products = visibleProducts.filter((product) => selected.includes(product.id))
    addProducts(products)
    setSelected([])
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
  const selectableResults = visibleProducts.filter((product) => Number(product.currentQuantity) > 0)

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
      <div className="flex flex-col gap-1">
        <Link
          to="/sales"
          className="inline-flex w-fit items-center gap-1.5 text-sm font-semibold text-primary hover:underline"
        >
          <IconArrowLeft size={16} />
          Back to Sales
        </Link>
        <h1 className="text-center text-lg font-semibold min-[481px]:text-xl md:text-2xl">New Sale</h1>
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
          <div className="relative w-full min-[481px]:flex-1">
            <input
              type="search"
              className="min-h-10 w-full rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              placeholder="Search products by name or SKU…"
              value={search}
              onFocus={() => setDropdownOpen(true)}
              onKeyDown={(event) => {
                if (event.key === 'Escape') setDropdownOpen(false)
              }}
              onChange={(event) => {
                setSearch(event.target.value)
                setSelected([])
                setDropdownOpen(Boolean(event.target.value.trim()))
              }}
            />

            {dropdownOpen && (
              <>
                <div className="fixed inset-0" aria-hidden="true" onClick={() => setDropdownOpen(false)} />
                <div className="absolute inset-x-0 top-full z-10 mt-2 overflow-hidden rounded-lg border border-border bg-surface shadow-lg">
                  {search.trim() ? (
                    searching ? (
                      <p className="m-0 p-4 text-sm text-secondary">Searching…</p>
                    ) : results.length === 0 ? (
                      <p className="m-0 p-4 text-sm text-secondary">No products match “{search.trim()}”.</p>
                    ) : (
                      <ProductSuggestions
                        products={results}
                        selected={selected}
                        lines={lines}
                        selectableResults={selectableResults}
                        onToggle={toggleSelect}
                        onToggleAll={toggleSelectAll}
                        onAddSelected={addSelected}
                        onAdd={addProduct}
                      />
                    )
                  ) : (
                    <>
                      <div className="border-b border-border bg-bg px-3 py-2">
                        <div className="text-sm font-semibold">Popular products</div>
                        <div className="text-xs text-secondary">Top sellers — click to add</div>
                      </div>
                      {popular === null ? (
                        <p className="m-0 p-4 text-sm text-secondary">Loading…</p>
                      ) : popular.length === 0 ? (
                        <p className="m-0 p-4 text-sm text-secondary">
                          No popular products yet. Search to add items.
                        </p>
                      ) : (
                        <ProductSuggestions
                          products={popular}
                          selected={selected}
                          lines={lines}
                          selectableResults={selectableResults}
                          onToggle={toggleSelect}
                          onToggleAll={toggleSelectAll}
                          onAddSelected={addSelected}
                          onAdd={addProduct}
                        />
                      )}
                    </>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      <div className="rounded-lg border border-border bg-surface p-4 shadow-sm md:p-6">
        <h2 className="mb-4 text-base font-semibold">Sale items</h2>

        {lines.length === 0 ? (
          <p className="p-6 text-center text-secondary">
            No items yet. Search and add products above.
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[600px] border-collapse [&_thead_tr]:bg-bg [&_tbody_tr:last-child_td]:border-b-0 [&_tbody_tr:hover]:bg-bg md:min-w-0">
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
                        min={COUNT_UNITS.has(line.unit) ? '1' : '0.001'}
                        step={COUNT_UNITS.has(line.unit) ? '1' : 'any'}
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

function ProductSuggestions({ products, selected, lines, selectableResults, onToggle, onToggleAll, onAddSelected, onAdd }) {
  return (
    <>
      <ul className="m-0 max-h-[45vh] list-none overflow-y-auto p-0 min-[481px]:max-h-72">
        {products.map((product) => {
          const outOfStock = Number(product.currentQuantity) <= 0
          const isSelected = selected.includes(product.id)
          const inSale = lines.some((line) => line.productId === product.id)
          return (
            <li
              className={`flex items-start justify-between gap-3 border-b border-border px-3 py-2 last:border-b-0 md:items-center ${isSelected ? 'bg-primary-light/60' : 'bg-surface'}`}
              key={product.id}
            >
              <div className="flex flex-1 items-start gap-3 md:items-center">
                <input
                  type="checkbox"
                  className="mt-1 h-4 w-4 shrink-0 cursor-pointer accent-primary disabled:cursor-not-allowed md:mt-0"
                  checked={isSelected}
                  disabled={outOfStock}
                  onChange={() => onToggle(product.id)}
                  aria-label={`Select ${product.name}`}
                />
                <div>
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="font-semibold">{product.name}</span>
                    {inSale && (
                      <span className="rounded-full bg-primary-light px-2 py-0.5 text-[11px] font-semibold whitespace-nowrap text-[#166534]">
                        ✓ In sale
                      </span>
                    )}
                  </div>
                  <div className="text-xs text-secondary">
                    {product.brand ? `${product.brand} · ` : ''}
                    {product.currentQuantity} {UNIT_LABELS[product.unit] ?? product.unit} ·{' '}
                    {product.mrp != null && product.mrp > 0 && (
                      <span className="text-muted line-through">{formatCurrency(product.mrp)} </span>
                    )}
                    {formatCurrency(product.sellingPrice)}
                  </div>
                </div>
              </div>
              <div className="flex items-center gap-2 whitespace-nowrap">
                <StockStatusBadge status={product.stockStatus} />
                <button
                  type="button"
                  className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-transparent bg-primary px-3 py-1 text-[13px] font-semibold text-white transition-colors hover:enabled:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                  disabled={outOfStock}
                  onClick={() => onAdd(product)}
                >
                  {outOfStock ? 'Out of stock' : inSale ? 'Add more' : 'Add'}
                </button>
              </div>
            </li>
          )
        })}
      </ul>
      <div className="flex flex-wrap items-center justify-between gap-2 border-t border-border bg-bg px-3 py-2">
        <label className="flex cursor-pointer items-center gap-2 text-sm text-secondary">
          <input
            type="checkbox"
            className="h-4 w-4 shrink-0 cursor-pointer accent-primary"
            checked={
              selectableResults.length > 0 &&
              selectableResults.every((product) => selected.includes(product.id))
            }
            disabled={selectableResults.length === 0}
            onChange={onToggleAll}
          />
          Select all ({selectableResults.length})
        </label>
        <button
          type="button"
          className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-transparent bg-primary px-3 py-1 text-[13px] font-semibold text-white transition-colors hover:enabled:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
          disabled={selected.length === 0}
          onClick={onAddSelected}
        >
          Add selected ({selected.length})
        </button>
      </div>
    </>
  )
}
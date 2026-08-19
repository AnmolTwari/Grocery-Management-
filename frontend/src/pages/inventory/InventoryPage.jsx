import { useEffect, useState } from 'react'
import RefreshButton from '../../components/RefreshButton'
import StockStatusBadge from '../../components/StockStatusBadge'
import { api } from '../../services/api'
import { listProducts } from '../../services/products'
import { adjustStock, listMovements, stockIn } from '../../services/inventory'
import { formatDateTime, formatQuantity, toNumber } from '../../utils/format'
import { MOVEMENT_TYPE_LABELS, UNIT_LABELS } from '../../utils/units'

const PAGE_SIZE = 20

const COUNT_UNITS = new Set(['PIECE', 'PACKET', 'BOX', 'BOTTLE'])

function quantityLabel(product) {
  return `${product.currentQuantity} ${UNIT_LABELS[product.unit] ?? product.unit}`
}

export default function InventoryPage() {
  const [products, setProducts] = useState([])
  const [movements, setMovements] = useState(null)

  const [mode, setMode] = useState('STOCK_IN')
  const [form, setForm] = useState({ productId: '', quantity: '' })
  const [submitting, setSubmitting] = useState(false)

  const [filterProductId, setFilterProductId] = useState('')
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)
  const [reload, setReload] = useState(0)

  const selectedProduct = products.find((p) => String(p.id) === form.productId)

  useEffect(() => {
    let cancelled = false
    listProducts({ page: 0, size: 100 })
      .then((data) => {
        if (!cancelled) setProducts(data.content)
      })
      .catch((err) => {
        if (!cancelled) setError(err.message)
      })
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    listMovements({
      productId: filterProductId || undefined,
      page,
      size: PAGE_SIZE,
    })
      .then((data) => {
        if (cancelled) return
        if (data.empty && page > 0) {
          setPage(page - 1)
          return
        }
        setMovements(data)
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
  }, [filterProductId, page, reload])

  function switchMode(nextMode) {
    if (nextMode === mode) return
    setMode(nextMode)
    setForm((current) => ({ ...current, quantity: '' }))
  }  const setField = (field) => (event) =>
    setForm((current) => ({ ...current, [field]: event.target.value }))

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setSuccess(null)

    const productId = Number(form.productId)
    if (!productId) {
      setError('Select a product.')
      return
    }
    const quantity = toNumber(form.quantity)
    if (mode === 'STOCK_IN' && quantity <= 0) {
      setError('Quantity to add must be greater than zero.')
      return
    }
    if (mode === 'ADJUSTMENT' && quantity < 0) {
      setError('Quantity cannot be negative.')
      return
    }
    if (selectedProduct && COUNT_UNITS.has(selectedProduct.unit) && !Number.isInteger(quantity)) {
      setError(
        `Quantity must be a whole number (${selectedProduct.unit === 'PIECE' ? 'pieces' : selectedProduct.unit.toLowerCase() + 's'}).`,
      )
      return
    }

    const payload =
      mode === 'STOCK_IN'
        ? { productId, quantity }
        : { productId, newQuantity: quantity }

    setSubmitting(true)
    try {
      if (mode === 'STOCK_IN') {
        await stockIn(payload)
      } else {
        await adjustStock(payload)
      }
      setForm((current) => ({ ...current, quantity: '' }))
      setPage(0)
      setSuccess(mode === 'STOCK_IN' ? 'Stock added.' : 'Stock updated.')
      setReload((value) => value + 1)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  function handleRefresh() {
    api.clearCache()
    setReload((value) => value + 1)
  }

  const isEmpty = movements && movements.content.length === 0

  return (
    <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-lg font-semibold min-[481px]:text-xl md:text-2xl">Inventory</h1>
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

      <div
        className="flex w-full gap-1 rounded-lg border border-border bg-bg p-1 md:w-auto"
        role="tablist"
        aria-label="Stock operation"
      >
        <button
          type="button"
          className={`min-h-10 flex-1 cursor-pointer rounded-md border-none px-4 py-2 text-sm font-semibold transition-colors md:flex-none ${mode === 'STOCK_IN' ? 'bg-primary text-white shadow-sm' : 'text-secondary hover:bg-surface'}`}
          onClick={() => switchMode('STOCK_IN')}
        >
          ＋ Add Stock
        </button>
        <button
          type="button"
          className={`min-h-10 flex-1 cursor-pointer rounded-md border-none px-4 py-2 text-sm font-semibold transition-colors md:flex-none ${mode === 'ADJUSTMENT' ? 'bg-primary text-white shadow-sm' : 'text-secondary hover:bg-surface'}`}
          onClick={() => switchMode('ADJUSTMENT')}
        >
          Update Stock
        </button>
      </div>

      <form className="max-w-[720px] rounded-lg border border-border bg-surface p-4 shadow-sm md:p-6" onSubmit={handleSubmit}>
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="inventory-product">
              Product *
            </label>
            <select
              id="inventory-product"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              value={form.productId}
              onChange={setField('productId')}
            >
              <option value="">Select a product</option>
              {products.map((product) => (
                <option key={product.id} value={product.id}>
                  {product.name}
                  {product.sku ? ` (${product.sku})` : ''}
                </option>
              ))}
            </select>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="inventory-quantity">
              {mode === 'STOCK_IN' ? 'Quantity to add *' : 'New quantity *'}
            </label>
            <input
              id="inventory-quantity"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              type="number"
              min={mode === 'STOCK_IN' ? (selectedProduct && COUNT_UNITS.has(selectedProduct.unit) ? '1' : '0.001') : '0'}
              step={selectedProduct && COUNT_UNITS.has(selectedProduct.unit) ? '1' : 'any'}
              placeholder={mode === 'STOCK_IN' ? 'e.g. 20' : 'e.g. 100'}
              value={form.quantity}
              onChange={setField('quantity')}
              required
            />
            {selectedProduct && (
              <small className="text-[11px] text-secondary min-[481px]:text-xs">
                Current stock: {quantityLabel(selectedProduct)}
              </small>
            )}
          </div>
        </div>

        <div className="mt-6 flex justify-end">
          <button
            type="submit"
            className="inline-flex min-h-10 w-full cursor-pointer items-center justify-center gap-2 rounded-sm border border-transparent bg-primary px-4 py-2 text-sm font-semibold text-white transition-colors hover:enabled:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0 md:w-auto"
            disabled={submitting}
          >
            {submitting
              ? mode === 'STOCK_IN'
                ? 'Adding…'
                : 'Updating…'
              : mode === 'STOCK_IN'
                ? 'Add Stock'
                : 'Update Stock'}
          </button>
        </div>
      </form>

      <div className="flex flex-col gap-2 min-[481px]:flex-row min-[481px]:items-center min-[481px]:justify-between">
        <div className="flex items-center gap-2">
          <h2 className="m-0 text-base font-semibold">Movement history</h2>
          <RefreshButton onClick={handleRefresh} disabled={loading} />
        </div>
        <select
          className="min-h-10 w-full rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary min-[481px]:w-52"
          value={filterProductId}
          onChange={(event) => {
            setPage(0)
            setFilterProductId(event.target.value)
          }}
        >
          <option value="">All products</option>
          {products.map((product) => (
            <option key={product.id} value={product.id}>
              {product.name}
            </option>
          ))}
        </select>
      </div>

      {isEmpty && !loading && (
        <div className="rounded-lg border border-dashed border-border bg-surface p-10 text-center text-secondary shadow-sm md:p-14">
          <p>No stock movements yet. Use the form above to add or update stock.</p>
        </div>
      )}

      {movements && !isEmpty && (
        <>
          <div className="overflow-x-auto rounded-lg border border-border bg-surface shadow-sm">
            <table className="w-full min-w-[600px] border-collapse [&_thead_tr]:bg-bg [&_tbody_tr:last-child_td]:border-b-0 [&_tbody_tr:hover]:bg-bg md:min-w-0">
              <thead>
                <tr>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Date &amp; Time
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Product
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Type
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Previous
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Change
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    New Stock
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Status
                  </th>
                </tr>
              </thead>
              <tbody>
                {movements.content.map((movement) => (
                  <tr key={movement.id}>
                    <td className="border-b border-border p-3 text-left align-middle">
                      {formatDateTime(movement.createdAt)}
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      <div className="font-semibold">{movement.productName}</div>
                      {movement.productSku && (
                        <div className="text-xs text-secondary">{movement.productSku}</div>
                      )}
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      <span
                        className={`inline-block rounded-full px-2 py-0.5 text-xs font-semibold whitespace-nowrap ${
                          movement.type === 'STOCK_IN'
                            ? 'bg-primary-light text-[#166534]'
                            : movement.type === 'ADJUSTMENT'
                              ? 'bg-[#e0f2fe] text-[#075985]'
                              : 'bg-[#ede9fe] text-[#5b21b6]'
                        }`}
                      >
                        {MOVEMENT_TYPE_LABELS[movement.type] ?? movement.type}
                      </span>
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      {formatQuantity(movement.previousQuantity)} {UNIT_LABELS[movement.unit] ?? movement.unit}
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      <ChangeCell value={movement.quantityChanged} unit={UNIT_LABELS[movement.unit] ?? movement.unit} />
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      {formatQuantity(movement.newQuantity)} {UNIT_LABELS[movement.unit] ?? movement.unit}
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      <StockStatusBadge status={movement.stockStatus} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {movements.totalPages > 1 && (
            <div className="flex items-center justify-center gap-3">
              <button
                type="button"
                className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                disabled={movements.first}
                onClick={() => setPage(page - 1)}
              >
                Prev
              </button>
              <span className="text-sm text-secondary">
                Page {movements.number + 1} of {movements.totalPages}
              </span>
              <button
                type="button"
                className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                disabled={movements.last}
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

function ChangeCell({ value, unit }) {
  if (value === null || value === undefined) return null
  const numeric = Number(value)
  const className =
    numeric > 0 ? 'font-semibold text-[#166534]' : numeric < 0 ? 'font-semibold text-[#991b1b]' : 'text-muted'
  const prefix = numeric > 0 ? '+' : numeric < 0 ? '−' : ''
  return (
    <span className={className}>
      {prefix}
      {formatQuantity(Math.abs(numeric))} {unit}
    </span>
  )
}
import { useEffect, useState } from 'react'
import { listProducts } from '../../services/products'
import { adjustStock, listMovements, stockIn } from '../../services/inventory'
import { formatDateTime, toNumber } from '../../utils/format'
import { MOVEMENT_TYPE_LABELS, UNIT_LABELS } from '../../utils/units'

const PAGE_SIZE = 20

function quantityLabel(product) {
  return `${product.currentQuantity} ${UNIT_LABELS[product.unit] ?? product.unit}`
}

export default function InventoryPage() {
  const [products, setProducts] = useState([])
  const [movements, setMovements] = useState(null)

  const [mode, setMode] = useState('STOCK_IN')
  const [form, setForm] = useState({ productId: '', quantity: '', reason: '' })
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
  }

  const setField = (field) => (event) =>
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

    const reason = form.reason.trim() || null
    const payload =
      mode === 'STOCK_IN'
        ? { productId, quantity, reason }
        : { productId, newQuantity: quantity, reason }

    setSubmitting(true)
    try {
      if (mode === 'STOCK_IN') {
        await stockIn(payload)
      } else {
        await adjustStock(payload)
      }
      setForm((current) => ({ ...current, quantity: '', reason: '' }))
      setPage(0)
      setSuccess(mode === 'STOCK_IN' ? 'Stock added.' : 'Stock adjusted.')
      setReload((value) => value + 1)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const isEmpty = movements && movements.content.length === 0

  return (
    <div className="content">
      <div className="page-header">
        <h1 className="page-title">Inventory</h1>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}
      {success && !error && <div className="alert alert-success">{success}</div>}

      <div className="segmented" role="tablist" aria-label="Stock operation">
        <button
          type="button"
          className={`seg-btn${mode === 'STOCK_IN' ? ' active' : ''}`}
          onClick={() => switchMode('STOCK_IN')}
        >
          ＋ Stock In
        </button>
        <button
          type="button"
          className={`seg-btn${mode === 'ADJUSTMENT' ? ' active' : ''}`}
          onClick={() => switchMode('ADJUSTMENT')}
        >
          Adjust Stock
        </button>
      </div>

      <form className="card form" onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="field">
            <label className="label" htmlFor="inventory-product">
              Product *
            </label>
            <select
              id="inventory-product"
              className="input"
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

          <div className="field">
            <label className="label" htmlFor="inventory-quantity">
              {mode === 'STOCK_IN' ? 'Quantity to add *' : 'New quantity *'}
            </label>
            <input
              id="inventory-quantity"
              className="input"
              type="number"
              min={mode === 'STOCK_IN' ? '0.001' : '0'}
              step="any"
              value={form.quantity}
              onChange={setField('quantity')}
              required
            />
            {selectedProduct && (
              <small className="field-hint">
                Current stock: {quantityLabel(selectedProduct)}
              </small>
            )}
          </div>

          <div className="field field-full">
            <label className="label" htmlFor="inventory-reason">
              Reason
            </label>
            <input
              id="inventory-reason"
              className="input"
              placeholder="e.g. Purchase, damaged items"
              value={form.reason}
              onChange={setField('reason')}
              maxLength={200}
            />
          </div>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? 'Saving…' : mode === 'STOCK_IN' ? 'Add Stock' : 'Save Adjustment'}
          </button>
        </div>
      </form>

      <h2 className="section-title">Movement history</h2>

      <div className="toolbar">
        <select
          className="input"
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
        <div className="card empty-state">
          <p>No stock movements yet. Use a form above to add or adjust stock.</p>
        </div>
      )}

      {movements && !isEmpty && (
        <>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Date &amp; Time</th>
                  <th>Product</th>
                  <th>Type</th>
                  <th>Previous</th>
                  <th>Change</th>
                  <th>New Stock</th>
                  <th>Reason</th>
                </tr>
              </thead>
              <tbody>
                {movements.content.map((movement) => (
                  <tr key={movement.id}>
                    <td>{formatDateTime(movement.createdAt)}</td>
                    <td>
                      <div className="product-name">{movement.productName}</div>
                      {movement.productSku && (
                        <div className="product-brand">{movement.productSku}</div>
                      )}
                    </td>
                    <td>
                      <span className={`badge badge-${String(movement.type).toLowerCase()}`}>
                        {MOVEMENT_TYPE_LABELS[movement.type] ?? movement.type}
                      </span>
                    </td>
                    <td>
                      {movement.previousQuantity} {UNIT_LABELS[movement.unit] ?? movement.unit}
                    </td>
                    <td>
                      <ChangeCell value={movement.quantityChanged} unit={UNIT_LABELS[movement.unit] ?? movement.unit} />
                    </td>
                    <td>
                      {movement.newQuantity} {UNIT_LABELS[movement.unit] ?? movement.unit}
                    </td>
                    <td>{movement.reason}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {movements.totalPages > 1 && (
            <div className="pagination">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                disabled={movements.first}
                onClick={() => setPage(page - 1)}
              >
                Prev
              </button>
              <span className="pagination-info">
                Page {movements.number + 1} of {movements.totalPages}
              </span>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                disabled={movements.last}
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

function ChangeCell({ value, unit }) {
  if (value === null || value === undefined) return null
  const numeric = Number(value)
  const className =
    numeric > 0 ? 'change-positive' : numeric < 0 ? 'change-negative' : 'change-zero'
  const prefix = numeric > 0 ? '+' : numeric < 0 ? '−' : ''
  return (
    <span className={className}>
      {prefix}
      {numeric} {unit}
    </span>
  )
}
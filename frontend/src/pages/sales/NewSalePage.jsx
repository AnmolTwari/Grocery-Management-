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
    <div className="content">
      <div className="page-header">
        <h1 className="page-title">New Sale</h1>
        <Link to="/sales" className="btn btn-secondary btn-sm">
          ← Back to Sales
        </Link>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}
      {success && !error && <div className="alert alert-success">{success}</div>}

      <div className="card">
        <h2 className="section-title">Add items</h2>
        <div className="toolbar">
          <input
            type="search"
            className="input"
            placeholder="Search products by name or SKU…"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        {searching && <p className="loading-text">Searching…</p>}
        {!searching && search.trim() && results.length === 0 && (
          <p className="loading-text">No products match “{search.trim()}”.</p>
        )}
        {results.length > 0 && (
          <ul className="search-results">
            {results.map((product) => {
              const outOfStock = Number(product.currentQuantity) <= 0
              return (
                <li key={product.id}>
                  <div>
                    <div className="product-name">{product.name}</div>
                    <div className="product-brand">
                      {product.brand ? `${product.brand} · ` : ''}
                      {product.currentQuantity} {UNIT_LABELS[product.unit] ?? product.unit} ·{' '}
                      {formatCurrency(product.sellingPrice)}
                    </div>
                  </div>
                  <div className="actions">
                    <StockStatusBadge status={product.stockStatus} />
                    <button
                      type="button"
                      className="btn btn-primary btn-sm"
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

      <div className="card mt-4">
        <h2 className="section-title">Sale items</h2>

        {lines.length === 0 ? (
          <p className="empty-state" style={{ padding: 'var(--space-6)' }}>
            No items yet. Search and add products above.
          </p>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Unit Price</th>
                  <th>Qty</th>
                  <th>Line Total</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {lines.map((line, index) => (
                  <tr key={line.productId}>
                    <td>
                      <div className="product-name">{line.name}</div>
                      {line.sku && <div className="product-brand">{line.sku}</div>}
                    </td>
                    <td>{formatCurrency(line.unitPrice)}</td>
                    <td>
                      <input
                        className="input"
                        style={{ width: '90px' }}
                        type="number"
                        min="0.001"
                        step="any"
                        value={line.quantity}
                        onChange={(event) => setQuantity(index, event.target.value)}
                      />
                    </td>
                    <td>{formatCurrency(lineTotal(line))}</td>
                    <td className="actions">
                      <button type="button" className="btn btn-danger btn-sm" onClick={() => removeLine(index)}>
                        Remove
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="total-box">
          <span className="label">Total</span>
          <span className="total-amount">{formatCurrency(bagTotal)}</span>
        </div>

        <div className="form-actions">
          <button
            type="button"
            className="btn btn-secondary"
            disabled={lines.length === 0}
            onClick={() => setLines([])}
          >
            Clear
          </button>
          <button
            type="button"
            className="btn btn-primary"
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
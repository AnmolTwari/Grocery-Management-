import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { IconEdit, IconTrash } from '../../components/icons'
import StockStatusBadge from '../../components/StockStatusBadge'
import { listCategories } from '../../services/categories'
import { listProducts, removeProduct } from '../../services/products'
import { formatCurrency } from '../../utils/format'
import { STOCK_STATUS_LABELS, UNIT_LABELS } from '../../utils/units'

const PAGE_SIZE = 20

const STOCK_STATUS_OPTIONS = [
  { value: '', label: 'All stock' },
  { value: 'IN_STOCK', label: STOCK_STATUS_LABELS.IN_STOCK },
  { value: 'LOW_STOCK', label: STOCK_STATUS_LABELS.LOW_STOCK },
  { value: 'OUT_OF_STOCK', label: STOCK_STATUS_LABELS.OUT_OF_STOCK },
]

export default function ProductListPage() {
  const [search, setSearch] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [stockStatus, setStockStatus] = useState('')
  const [page, setPage] = useState(0)

  const [categories, setCategories] = useState([])
  const [products, setProducts] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)
  const [reload, setReload] = useState(0)

  useEffect(() => {
    let cancelled = false
    listCategories()
      .then((data) => {
        if (!cancelled) setCategories(data)
      })
      .catch(() => {

      })
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    const timer = setTimeout(async () => {
      try {
        setLoading(true)
        const data = await listProducts({
          search: search || undefined,
          categoryId: categoryId || undefined,
          stockStatus: stockStatus || undefined,
          page,
          size: PAGE_SIZE,
        })
        if (cancelled) return
        if (data.empty && page > 0) {
          setPage(page - 1)
          return
        }
        setProducts(data)
        setError(null)
      } catch (err) {
        if (!cancelled) setError(err.message)
      } finally {
        if (!cancelled) setLoading(false)
      }
    }, 250)
    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [search, categoryId, stockStatus, page, reload])

  async function handleRemove(product) {
    const confirmed = window.confirm(
      `Remove "${product.name}"?\n\nIf it has sale or stock history it will be hidden from the list; otherwise it is deleted permanently.`,
    )
    if (!confirmed) return
    try {
      await removeProduct(product.id)
      setSuccess(`"${product.name}" removed.`)
      setReload((value) => value + 1)
    } catch (err) {
      setError(err.message)
    }
  }

  const isEmpty = products && products.content.length === 0

  return (
    <div className="content">
      <div className="page-header">
        <h1 className="page-title">Products</h1>
        <Link to="/products/new" className="btn btn-primary">
          Add Product
        </Link>
      </div>

      <div className="toolbar">
        <input
          type="search"
          className="input"
          placeholder="Search products…"
          value={search}
          onChange={(event) => {
            setPage(0)
            setSearch(event.target.value)
          }}
        />
        <select
          className="input"
          value={categoryId}
          onChange={(event) => {
            setPage(0)
            setCategoryId(event.target.value)
          }}
        >
          <option value="">All categories</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
        <select
          className="input"
          value={stockStatus}
          onChange={(event) => {
            setPage(0)
            setStockStatus(event.target.value)
          }}
        >
          {STOCK_STATUS_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}
      {success && !error && <div className="alert alert-success">{success}</div>}

      {isEmpty && !loading && (
        <div className="card empty-state">
          <p>No products found. Add your first product.</p>
        </div>
      )}

      {products && !isEmpty && (
        <>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Category</th>
                  <th>Selling Price</th>
                  <th>Stock</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {products.content.map((product) => (
                  <tr key={product.id}>
                    <td>
                      <div className="product-name">{product.name}</div>
                      {product.brand && <div className="product-brand">{product.brand}</div>}
                      {product.sku && <div className="product-brand">{product.sku}</div>}
                    </td>
                    <td>{product.categoryName}</td>
                    <td>{formatCurrency(product.sellingPrice)}</td>
                    <td>
                      {product.currentQuantity} {UNIT_LABELS[product.unit] ?? product.unit}
                    </td>
                    <td>
                      <StockStatusBadge status={product.stockStatus} />
                    </td>
                    <td className="actions">
                      <Link
                        to={`/products/${product.id}/edit`}
                        className="btn-icon"
                        title={`Edit ${product.name}`}
                        aria-label={`Edit ${product.name}`}
                      >
                        <IconEdit />
                      </Link>
                      <button
                        type="button"
                        className="btn-icon btn-icon-danger"
                        title={`Remove ${product.name}`}
                        aria-label={`Remove ${product.name}`}
                        onClick={() => handleRemove(product)}
                      >
                        <IconTrash />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {products.totalPages > 1 && (
            <div className="pagination">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                disabled={products.first}
                onClick={() => setPage(page - 1)}
              >
                Prev
              </button>
              <span className="pagination-info">
                Page {products.number + 1} of {products.totalPages}
              </span>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                disabled={products.last}
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
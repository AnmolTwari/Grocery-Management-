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
    <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-lg min-[481px]:text-xl md:text-2xl">Products</h1>
        <Link
          to="/products/new"
          className="inline-flex min-h-10 shrink-0 cursor-pointer items-center justify-center gap-2 rounded-sm border border-transparent bg-primary px-4 py-2 text-sm font-semibold text-white transition-colors hover:enabled:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
        >
          Add Product
        </Link>
      </div>

      <div className="flex flex-col gap-2 min-[481px]:flex-row min-[481px]:flex-wrap">
        <input
          type="search"
          className="min-h-10 w-full rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary min-[481px]:min-w-40 min-[481px]:flex-1"
          placeholder="Search products…"
          value={search}
          onChange={(event) => {
            setPage(0)
            setSearch(event.target.value)
          }}
        />
        <select
          className="min-h-10 w-full rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary min-[481px]:min-w-40 min-[481px]:flex-1"
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
          className="min-h-10 w-full rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary min-[481px]:min-w-40 min-[481px]:flex-1"
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

      {isEmpty && !loading && (
        <div className="rounded-lg border border-border bg-surface p-8 text-center text-secondary shadow-sm">
          <p>No products found. Add your first product.</p>
        </div>
      )}

      {products && !isEmpty && (
        <>
          <div className="overflow-x-auto rounded-lg border border-border bg-surface shadow-sm">
            <table className="w-full min-w-[600px] border-collapse [&_tbody_tr:last-child_td]:border-b-0 [&_tbody_tr:hover]:bg-bg md:min-w-0">
              <thead>
                <tr>
                  <th
                    className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase"
                    style={{ width: '200px' }}
                  >
                    Product
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Category
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Selling Price
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Stock
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Status
                  </th>
                  <th className="border-b border-border p-3 text-left align-middle text-xs font-semibold tracking-wider text-muted uppercase">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody>
                {products.content.map((product) => (
                  <tr key={product.id}>
                    <td className="border-b border-border p-3 text-left align-middle">
                      <div className="flex flex-col">
                        <div className="font-semibold">{product.name}</div>
                        {product.brand && <div className="text-xs text-secondary">{product.brand}</div>}
                        {product.sku && <div className="text-xs text-secondary">{product.sku}</div>}
                      </div>
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      {product.categoryName}
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      {formatCurrency(product.sellingPrice)}
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      {product.currentQuantity} {UNIT_LABELS[product.unit] ?? product.unit}
                    </td>
                    <td className="border-b border-border p-3 text-left align-middle">
                      <StockStatusBadge status={product.stockStatus} />
                    </td>
                    <td className=" items-center gap-2 border-b border-border p-3 text-left align-middle whitespace-nowrap">
                      <Link
                        to={`/products/${product.id}/edit`}
                        className="mr-2.5 inline-flex h-10 w-10 shrink-0 cursor-pointer items-center justify-center rounded-sm border border-border bg-surface text-secondary transition-colors hover:bg-bg hover:text-text md:h-8 md:w-8"
                        title={`Edit ${product.name}`}
                        aria-label={`Edit ${product.name}`}
                      >
                        <IconEdit />
                      </Link>
                      <button
                        type="button"
                        className="mr-2.5 inline-flex h-10 w-10 shrink-0 cursor-pointer items-center justify-center rounded-sm border border-border bg-surface text-danger transition-colors hover:border-danger hover:bg-[#fef2f2] hover:text-danger md:h-8 md:w-8"
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
            <div className="flex items-center justify-center gap-3">
              <button
                type="button"
                className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                disabled={products.first}
                onClick={() => setPage(page - 1)}
              >
                Prev
              </button>
              <span className="text-sm text-secondary">
                Page {products.number + 1} of {products.totalPages}
              </span>
              <button
                type="button"
                className="inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-3 py-1 text-[13px] font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0"
                disabled={products.last}
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
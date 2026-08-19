import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createCategory, listCategories } from '../../services/categories'
import {
  createProduct,
  getProduct,
  updateProduct,
} from '../../services/products'
import { toNumber } from '../../utils/format'
import { UNIT_LABELS } from '../../utils/units'

const COUNT_UNITS = new Set(['PIECE', 'PACKET', 'BOX', 'BOTTLE'])

function isWholeNumber(value) {
  const numeric = Number(value)
  return Number.isFinite(numeric) && Number.isInteger(numeric)
}

const EMPTY_FORM = {
  name: '',
  categoryId: '',
  brand: '',
  sku: '',
  unit: 'PIECE',
  purchasePrice: '',
  sellingPrice: '',
  currentQuantity: '0',
  minimumStockLevel: '0',
  active: true,
}

export default function ProductFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)

  const [categories, setCategories] = useState([])
  const [newCategory, setNewCategory] = useState('')
  const [form, setForm] = useState(EMPTY_FORM)
  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    Promise.all([
      listCategories(),
      isEdit ? getProduct(id) : Promise.resolve(null),
    ])
      .then(([categoryList, product]) => {
        if (cancelled) return
        setCategories(categoryList)
        if (product) {
          setForm({
            name: product.name,
            categoryId: String(product.categoryId),
            brand: product.brand ?? '',
            sku: product.sku ?? '',
            unit: product.unit,
            purchasePrice: String(product.purchasePrice),
            sellingPrice: String(product.sellingPrice),
            currentQuantity: String(product.currentQuantity),
            minimumStockLevel: String(product.minimumStockLevel),
            active: product.active,
          })
        }
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
  }, [id, isEdit])

  const setField = (field) => (event) => {
    const value = event.target.type === 'checkbox' ? event.target.checked : event.target.value
    setForm((current) => ({ ...current, [field]: value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)

    let categoryId = form.categoryId
    try {
      if (categoryId === 'new') {
        const trimmed = newCategory.trim()
        if (!trimmed) {
          setError('Enter a name for the new category.')
          return
        }
        const category = await createCategory(trimmed)
        categoryId = String(category.id)
      }
      if (!categoryId) {
        setError('Please select or create a category.')
        return
      }

      if (COUNT_UNITS.has(form.unit)) {
        if (!isWholeNumber(form.currentQuantity) || !isWholeNumber(form.minimumStockLevel)) {
          setError('Quantities must be whole numbers for this product unit.')
          return
        }
      }

      const payload = {
        name: form.name,
        categoryId: Number(categoryId),
        brand: form.brand || null,
        sku: form.sku || null,
        unit: form.unit,
        purchasePrice: toNumber(form.purchasePrice),
        sellingPrice: toNumber(form.sellingPrice),
        currentQuantity: toNumber(form.currentQuantity),
        minimumStockLevel: toNumber(form.minimumStockLevel),
        active: form.active,
      }

      setSaving(true)
      if (isEdit) {
        await updateProduct(id, payload)
      } else {
        await createProduct(payload)
      }
      navigate('/products')
    } catch (err) {
      setError(err.message)
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
        <p className="text-secondary">Loading…</p>
      </div>
    )
  }

  return (
    <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-lg min-[481px]:text-xl md:text-2xl">
          {isEdit ? 'Edit Product' : 'Add Product'}
        </h1>
      </div>

      {error && (
        <div className="rounded-sm border border-[#fecaca] bg-[#fee2e2] px-4 py-3 text-sm text-[#991b1b]">
          {error}
        </div>
      )}

      <form
        className="max-w-[720px] rounded-lg border border-border bg-surface p-4 shadow-sm md:p-6"
        onSubmit={handleSubmit}
      >
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="name">
              Product name *
            </label>
            <input
              id="name"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              value={form.name}
              onChange={setField('name')}
              required
              maxLength={150}
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="category">
              Category *
            </label>
            <select
              id="category"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              value={form.categoryId}
              onChange={setField('categoryId')}
            >
              <option value="">Select a category</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
              <option value="new">＋ Add new category…</option>
            </select>
            {form.categoryId === 'new' && (
              <input
                className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
                placeholder="New category name"
                value={newCategory}
                onChange={(event) => setNewCategory(event.target.value)}
              />
            )}
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="brand">
              Brand
            </label>
            <input
              id="brand"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              value={form.brand}
              onChange={setField('brand')}
              maxLength={100}
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="sku">
              SKU / Code
            </label>
            <input
              id="sku"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              value={form.sku}
              onChange={setField('sku')}
              maxLength={50}
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="unit">
              Unit *
            </label>
            <select
              id="unit"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              value={form.unit}
              onChange={setField('unit')}
            >
              {Object.entries(UNIT_LABELS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="purchasePrice">
              Purchase price *
            </label>
            <input
              id="purchasePrice"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              type="number"
              min="0"
              step="0.01"
              value={form.purchasePrice}
              onChange={setField('purchasePrice')}
              required
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="sellingPrice">
              Selling price *
            </label>
            <input
              id="sellingPrice"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              type="number"
              min="0"
              step="0.01"
              value={form.sellingPrice}
              onChange={setField('sellingPrice')}
              required
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="currentQuantity">
              Current quantity
            </label>
            <input
              id="currentQuantity"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              type="number"
              min="0"
              step={COUNT_UNITS.has(form.unit) ? '1' : 'any'}
              value={form.currentQuantity}
              onChange={setField('currentQuantity')}
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="minimumStockLevel">
              Minimum stock level
            </label>
            <input
              id="minimumStockLevel"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              type="number"
              min="0"
              step={COUNT_UNITS.has(form.unit) ? '1' : 'any'}
              value={form.minimumStockLevel}
              onChange={setField('minimumStockLevel')}
            />
          </div>

          <div className="flex flex-col gap-1">
            <span className="text-sm font-semibold" id="status-label">
              Status
            </span>
            <label className="flex items-center gap-2 pt-2 text-sm" htmlFor="active">
              <input
                id="active"
                type="checkbox"
                checked={form.active}
                onChange={setField('active')}
              />
              Active (shown in the product list)
            </label>
          </div>
        </div>

        <div className="mt-6 flex flex-col-reverse gap-2 md:flex-row md:justify-end">
          <button
            type="button"
            className="inline-flex min-h-10 w-full cursor-pointer items-center justify-center gap-2 rounded-sm border border-border bg-surface px-4 py-2 text-sm font-semibold text-text transition-colors hover:enabled:bg-bg disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0 md:w-auto"
            onClick={() => navigate('/products')}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="inline-flex min-h-10 w-full cursor-pointer items-center justify-center gap-2 rounded-sm border border-transparent bg-primary px-4 py-2 text-sm font-semibold text-white transition-colors hover:enabled:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0 md:w-auto"
            disabled={saving}
          >
            {saving ? 'Saving…' : isEdit ? 'Save Changes' : 'Save Product'}
          </button>
        </div>
      </form>
    </div>
  )
}
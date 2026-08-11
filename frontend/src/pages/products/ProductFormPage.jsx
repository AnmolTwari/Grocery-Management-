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
      <div className="content">
        <p className="loading-text">Loading…</p>
      </div>
    )
  }

  return (
    <div className="content">
      <div className="page-header">
        <h1 className="page-title">{isEdit ? 'Edit Product' : 'Add Product'}</h1>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      <form className="card form" onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="field">
            <label className="label" htmlFor="name">
              Product name *
            </label>
            <input
              id="name"
              className="input"
              value={form.name}
              onChange={setField('name')}
              required
              maxLength={150}
            />
          </div>

          <div className="field">
            <label className="label" htmlFor="category">
              Category *
            </label>
            <select id="category" className="input" value={form.categoryId} onChange={setField('categoryId')}>
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
                className="input"
                placeholder="New category name"
                value={newCategory}
                onChange={(event) => setNewCategory(event.target.value)}
              />
            )}
          </div>

          <div className="field">
            <label className="label" htmlFor="brand">
              Brand
            </label>
            <input id="brand" className="input" value={form.brand} onChange={setField('brand')} maxLength={100} />
          </div>

          <div className="field">
            <label className="label" htmlFor="sku">
              SKU / Code
            </label>
            <input id="sku" className="input" value={form.sku} onChange={setField('sku')} maxLength={50} />
          </div>

          <div className="field">
            <label className="label" htmlFor="unit">
              Unit *
            </label>
            <select id="unit" className="input" value={form.unit} onChange={setField('unit')}>
              {Object.entries(UNIT_LABELS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          <div className="field">
            <label className="label" htmlFor="purchasePrice">
              Purchase price *
            </label>
            <input
              id="purchasePrice"
              className="input"
              type="number"
              min="0"
              step="0.01"
              value={form.purchasePrice}
              onChange={setField('purchasePrice')}
              required
            />
          </div>

          <div className="field">
            <label className="label" htmlFor="sellingPrice">
              Selling price *
            </label>
            <input
              id="sellingPrice"
              className="input"
              type="number"
              min="0"
              step="0.01"
              value={form.sellingPrice}
              onChange={setField('sellingPrice')}
              required
            />
          </div>

          <div className="field">
            <label className="label" htmlFor="currentQuantity">
              Current quantity
            </label>
            <input
              id="currentQuantity"
              className="input"
              type="number"
              min="0"
              step="any"
              value={form.currentQuantity}
              onChange={setField('currentQuantity')}
            />
          </div>

          <div className="field">
            <label className="label" htmlFor="minimumStockLevel">
              Minimum stock level
            </label>
            <input
              id="minimumStockLevel"
              className="input"
              type="number"
              min="0"
              step="any"
              value={form.minimumStockLevel}
              onChange={setField('minimumStockLevel')}
            />
          </div>

          <div className="field">
            <span className="label" id="status-label">
              Status
            </span>
            <label className="field-checkbox" htmlFor="active">
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

        <div className="form-actions">
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/products')}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving…' : isEdit ? 'Save Changes' : 'Save Product'}
          </button>
        </div>
      </form>
    </div>
  )
}
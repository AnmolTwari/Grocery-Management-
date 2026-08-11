import { api } from './api'

export function stockIn(data) {
  return api.post('/inventory/stock-in', data)
}

export function adjustStock(data) {
  return api.post('/inventory/adjustment', data)
}

export function listMovements({ productId, page, size } = {}) {
  const params = new URLSearchParams()
  if (productId) params.set('productId', productId)
  if (page != null) params.set('page', page)
  if (size != null) params.set('size', size)
  const query = params.toString()
  return api.get(`/inventory/movements${query ? `?${query}` : ''}`)
}
import { api } from './api'

export function listProducts({ search, categoryId, stockStatus, page, size } = {}) {
  const params = new URLSearchParams()
  if (search) params.set('search', search)
  if (categoryId) params.set('categoryId', categoryId)
  if (stockStatus) params.set('stockStatus', stockStatus)
  if (page != null) params.set('page', page)
  if (size != null) params.set('size', size)
  const query = params.toString()
  return api.get(`/products${query ? `?${query}` : ''}`)
}

export const getProduct = (id) => api.get(`/products/${id}`)

export const createProduct = (data) => api.post('/products', data)

export const updateProduct = (id, data) => api.put(`/products/${id}`, data)

export const removeProduct = (id) => api.delete(`/products/${id}`)
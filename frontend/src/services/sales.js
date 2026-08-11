import { api } from './api'

export function listSales({ page, size } = {}) {
  const params = new URLSearchParams()
  if (page != null) params.set('page', page)
  if (size != null) params.set('size', size)
  const query = params.toString()
  return api.get(`/sales${query ? `?${query}` : ''}`)
}

export const getSale = (id) => api.get(`/sales/${id}`)

export const createSale = (data) => api.post('/sales', data)
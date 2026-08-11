import { api } from './api'

export const listCategories = () => api.get('/categories')

export const createCategory = (name) => api.post('/categories', { name })
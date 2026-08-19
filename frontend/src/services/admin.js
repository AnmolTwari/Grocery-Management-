import { api } from './api'

export function listUsers({ page = 0, size = 20 } = {}) {
  return api.get(`/admin/users?page=${page}&size=${size}`)
}

export function createUser({ username, email, password, name, role }) {
  const body = { username, password, role }
  if (email) body.email = email
  if (name) body.name = name
  return api.post('/admin/users', body)
}

export function updateUser(id, { role, enabled } = {}) {
  const body = {}
  if (role) body.role = role
  if (typeof enabled === 'boolean') body.enabled = enabled
  return api.patch(`/admin/users/${id}`, body)
}

export function deleteUser(id) {
  return api.delete(`/admin/users/${id}`)
}

export function getAnalytics() {
  return api.get('/admin/analytics')
}

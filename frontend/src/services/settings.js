import { api } from './api'

export const changePassword = (currentPassword, newPassword) =>
  api.post('/settings/password', { currentPassword, newPassword })

export const changeEmail = (email) => api.post('/settings/email', { email })

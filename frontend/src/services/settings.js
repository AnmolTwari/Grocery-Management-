import { api } from './api'

export const changePassword = (currentPassword, newPassword) =>
  api.post('/settings/password', { currentPassword, newPassword })

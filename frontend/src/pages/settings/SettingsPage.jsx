import { useState } from 'react'
import { auth } from '../../services/api'
import { changePassword } from '../../services/settings'

function usernameFromToken() {
  const token = auth.getToken()
  if (!token) return ''
  try {
    const payload = token.split('.')[1]
    const json = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))
    return json.sub || ''
  } catch {
    return ''
  }
}

export default function SettingsPage() {
  const [username] = useState(usernameFromToken)
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  function validate() {
    const errors = {}
    if (!currentPassword) errors.currentPassword = 'Current password is required'
    if (!newPassword) errors.newPassword = 'New password is required'
    else if (newPassword.length < 6) errors.newPassword = 'New password must be at least 6 characters'
    if (newPassword && currentPassword && newPassword === currentPassword) {
      errors.newPassword = 'New password must be different from the current one'
    }
    if (confirmPassword !== newPassword) errors.confirmPassword = 'Passwords do not match'
    setFieldErrors(errors)
    return Object.keys(errors).length === 0
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSuccess('')
    if (!validate()) return
    setLoading(true)
    try {
      await changePassword(currentPassword, newPassword)
      setSuccess('Password updated successfully.')
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
      setFieldErrors({})
    } catch (err) {
      if (err.status === 400 && err.message.includes('Current password')) {
        setFieldErrors({ currentPassword: err.message })
      } else {
        setError(err.message)
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="content">
      <div className="page-header">
        <h1 className="page-title">Settings</h1>
      </div>

      <div className="card card-wide">
        <h2 className="section-title">Account</h2>
        <div className="field">
          <span className="label">Username</span>
          <div className="input-static">{username || '—'}</div>
        </div>
      </div>

      <div className="card card-wide">
        <h2 className="section-title">Change Password</h2>
        <form onSubmit={handleSubmit} noValidate>
          <div className="field">
            <label className="label" htmlFor="current-password">
              Current password
            </label>
            <input
              id="current-password"
              className="input"
              type={showPassword ? 'text' : 'password'}
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
            />
            {fieldErrors.currentPassword && (
              <div className="field-error">{fieldErrors.currentPassword}</div>
            )}
          </div>

          <div className="field">
            <label className="label" htmlFor="new-password">
              New password
            </label>
            <input
              id="new-password"
              className="input"
              type={showPassword ? 'text' : 'password'}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
            />
            {fieldErrors.newPassword && (
              <div className="field-error">{fieldErrors.newPassword}</div>
            )}
          </div>

          <div className="field">
            <label className="label" htmlFor="confirm-password">
              Confirm new password
            </label>
            <input
              id="confirm-password"
              className="input"
              type={showPassword ? 'text' : 'password'}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
            {fieldErrors.confirmPassword && (
              <div className="field-error">{fieldErrors.confirmPassword}</div>
            )}
          </div>

          <label className="toggle-password">
            <input
              type="checkbox"
              checked={showPassword}
              onChange={(e) => setShowPassword(e.target.checked)}
            />
            Show passwords
          </label>

          {error && <div className="alert alert-danger">{error}</div>}
          {success && <div className="alert alert-success">{success}</div>}

          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving…' : 'Update Password'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
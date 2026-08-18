import { useEffect, useState } from 'react'
import { auth } from '../../services/api'
import { changeEmail, changePassword } from '../../services/settings'

export default function SettingsPage() {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    auth.me()
      .then((data) => {
        setUsername(data.username || '')
        setEmail(data.email || '')
      })
      .catch(() => {
        setUsername('')
      })
  }, [])

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

  const [emailSaving, setEmailSaving] = useState(false)
  const [emailError, setEmailError] = useState('')
  const [emailFieldError, setEmailFieldError] = useState('')

  async function handleEmailSubmit(e) {
    e.preventDefault()
    setEmailError('')
    setEmailFieldError('')
    const mail = email.trim()
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(mail)) {
      setEmailFieldError('Enter a valid email address.')
      return
    }
    setEmailSaving(true)
    try {
      const data = await changeEmail(mail)
      setEmail(data.email)
      setSuccess('Email updated successfully.')
    } catch (err) {
      setEmailError(err.message)
    } finally {
      setEmailSaving(false)
    }
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
    <div className="mx-auto flex w-full max-w-[1180px] flex-1 flex-col gap-4 p-3 px-4 pb-10 md:p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-lg min-[481px]:text-xl md:text-2xl">Settings</h1>
      </div>

      <div className="rounded-lg border border-border bg-surface p-4 shadow-sm md:p-6">
        <h2 className="mb-4 text-base font-semibold">Account</h2>
        <div className="flex max-w-[480px] flex-col gap-4">
          <div className="flex flex-col gap-1">
            <span className="text-sm font-semibold">Username</span>
            <div className="py-2 text-text">{username || '—'}</div>
          </div>
          <form className="flex flex-col gap-2" onSubmit={handleEmailSubmit} noValidate>
            <div className="flex flex-col gap-1">
              <label className="text-sm font-semibold" htmlFor="account-email">
                Email
              </label>
              <input
                id="account-email"
                className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
                type="email"
                autoComplete="email"
                placeholder="e.g. you@shop.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                aria-invalid={Boolean(emailFieldError)}
              />
              {emailFieldError && (
                <div className="text-[13px] text-danger">{emailFieldError}</div>
              )}
              {emailError && (
                <div className="text-[13px] text-danger">{emailError}</div>
              )}
            </div>
            <div className="flex justify-end">
              <button
                type="submit"
                className="inline-flex min-h-10 w-full cursor-pointer items-center justify-center gap-2 rounded-sm border border-transparent bg-primary px-4 py-2 text-sm font-semibold text-white transition-colors hover:enabled:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0 md:w-auto"
                disabled={emailSaving}
              >
                {emailSaving ? 'Saving…' : 'Save Email'}
              </button>
            </div>
          </form>
        </div>
      </div>

      <div className="rounded-lg border border-border bg-surface p-4 shadow-sm md:p-6">
        <h2 className="mb-4 text-base font-semibold">Change Password</h2>
        <form className="form-stack max-w-[480px]" onSubmit={handleSubmit} noValidate>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="current-password">
              Current password
            </label>
            <input
              id="current-password"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              type={showPassword ? 'text' : 'password'}
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
            />
            {fieldErrors.currentPassword && (
              <div className="text-[13px] text-danger">{fieldErrors.currentPassword}</div>
            )}
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="new-password">
              New password
            </label>
            <input
              id="new-password"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              type={showPassword ? 'text' : 'password'}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
            />
            {fieldErrors.newPassword && (
              <div className="text-[13px] text-danger">{fieldErrors.newPassword}</div>
            )}
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold" htmlFor="confirm-password">
              Confirm new password
            </label>
            <input
              id="confirm-password"
              className="min-h-10 rounded-sm border border-border bg-surface px-3 py-2 text-sm text-text focus-visible:border-primary focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-primary"
              type={showPassword ? 'text' : 'password'}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
            {fieldErrors.confirmPassword && (
              <div className="text-[13px] text-danger">{fieldErrors.confirmPassword}</div>
            )}
          </div>

          <label className="flex items-center gap-2 pt-2 text-sm">
            <input
              type="checkbox"
              checked={showPassword}
              onChange={(e) => setShowPassword(e.target.checked)}
            />
            Show passwords
          </label>

          {error && (
            <div className="rounded-sm border border-[#fecaca] bg-[#fee2e2] px-4 py-3 text-sm text-[#991b1b]">
              {error}
            </div>
          )}
          {success && (
            <div className="rounded-sm border border-[#bbf7d0] bg-primary-light px-4 py-3 text-sm text-[#166534]">
              {success}
            </div>
          )}

          <div className="mt-6 flex flex-col-reverse gap-2 md:flex-row md:justify-end">
            <button
              type="submit"
              className="inline-flex min-h-10 w-full cursor-pointer items-center justify-center gap-2 rounded-sm border border-transparent bg-primary px-4 py-2 text-sm font-semibold text-white transition-colors hover:enabled:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60 md:min-h-0 md:w-auto"
              disabled={loading}
            >
              {loading ? 'Saving…' : 'Update Password'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
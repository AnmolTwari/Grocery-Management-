import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { auth } from '../services/api'
import './AuthPage.css'

export default function AuthPage({ mode }) {
  const navigate = useNavigate()
  const location = useLocation()
  const isRegister = mode === 'register'
  const justRegistered = Boolean(location.state?.registered)

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (auth.isAuthenticated()) {
    return <Navigate to="/" replace />
  }

  function validate() {
    const errors = {}
    const name = username.trim()
    if (name.length < 3 || name.length > 50) {
      errors.username = 'Username must be 3–50 characters.'
    }
    if (password.length < 6) {
      errors.password = 'Password must be at least 6 characters.'
    }
    if (isRegister && confirmPassword !== password) {
      errors.confirmPassword = 'Passwords do not match.'
    }
    setFieldErrors(errors)
    return Object.keys(errors).length === 0
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    if (!validate()) return

    setLoading(true)
    try {
      if (isRegister) {
        await auth.register(username.trim(), password)
        navigate('/login', { replace: true, state: { registered: true } })
      } else {
        await auth.login(username.trim(), password)
        navigate('/', { replace: true })
      }
    } catch (err) {
      if (err.status === 409) {
        setError('This username is already taken. Please choose another.')
      } else {
        setError(err.message || (isRegister ? 'Registration failed.' : 'Login failed.'))
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <div className="auth-brand">
          <img src="/logo.png" alt="Grocery Manager logo" className="brand-logo" />
        </div>
        <h1 className="auth-title">{isRegister ? 'Create your shop account' : 'Sign in to your shop'}</h1>
        <p className="auth-subtitle">
          {isRegister
            ? 'Register once and manage your shop’s products, stock and sales.'
            : 'Welcome back. Sign in to manage your shop.'}
        </p>

        {error && <div className="alert alert-danger auth-alert">{error}</div>}

        {justRegistered && !isRegister && (
          <div className="alert alert-success auth-alert">
            Account created successfully. Please sign in.
          </div>
        )}

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="field">
            <label className="label" htmlFor="username">
              Username
            </label>
            <input
              id="username"
              className="input"
              type="text"
              autoComplete="username"
              autoFocus
              placeholder="e.g. mohan_sweets"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              aria-invalid={Boolean(fieldErrors.username)}
            />
            {fieldErrors.username && <span className="field-error">{fieldErrors.username}</span>}
          </div>

          <div className="field">
            <label className="label" htmlFor="password">
              Password
            </label>
            <div className="password-wrap">
              <input
                id="password"
                className="input"
                type={showPassword ? 'text' : 'password'}
                autoComplete={isRegister ? 'new-password' : 'current-password'}
                placeholder={isRegister ? 'At least 6 characters' : 'Your password'}
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                aria-invalid={Boolean(fieldErrors.password)}
              />
              <button
                type="button"
                className="password-toggle"
                onClick={() => setShowPassword((value) => !value)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? 'Hide' : 'Show'}
              </button>
            </div>
            {fieldErrors.password && <span className="field-error">{fieldErrors.password}</span>}
          </div>

          {isRegister && (
            <div className="field">
              <label className="label" htmlFor="confirmPassword">
                Confirm password
              </label>
              <input
                id="confirmPassword"
                className="input"
                type={showPassword ? 'text' : 'password'}
                autoComplete="new-password"
                placeholder="Repeat your password"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                aria-invalid={Boolean(fieldErrors.confirmPassword)}
              />
              {fieldErrors.confirmPassword && (
                <span className="field-error">{fieldErrors.confirmPassword}</span>
              )}
            </div>
          )}

          <button type="submit" className="btn btn-primary auth-submit" disabled={loading}>
            {loading ? (isRegister ? 'Creating account…' : 'Signing in…') : isRegister ? 'Create Account' : 'Sign In'}
          </button>
        </form>

        <p className="auth-switch">
          {isRegister ? (
            <>
              Already have an account? <Link to="/login">Sign in</Link>
            </>
          ) : (
            <>
              New to Grocery Manager? <Link to="/register">Create an account</Link>
            </>
          )}
        </p>
      </div>
      <p className="auth-footer">Your shop data is private to your account.</p>
    </div>
  )
}
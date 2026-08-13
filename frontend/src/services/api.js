const API_PREFIX = '/api'
const TOKEN_KEY = 'auth_token'

function getAuthHeader() {
  const token = localStorage.getItem(TOKEN_KEY)
  return token ? { 'Authorization': `Bearer ${token}` } : {}
}

function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
}

async function request(path, { method = 'GET', body, ...options } = {}) {
  const headers = {
    ...getAuthHeader(),
    ...(body ? { 'Content-Type': 'application/json' } : {}),
    ...(options.headers || {})
  }

  const response = await fetch(`${API_PREFIX}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
    ...options,
  })

  if (response.status === 401 && !path.startsWith('/auth/')) {
    // Token expired or invalid - clear auth and redirect to login.
    // /auth/* keeps showing its own error (e.g. wrong credentials).
    clearAuth()
    window.location.href = '/login'
    throw new Error('Session expired. Please log in again.')
  }

  if (!response.ok) {
    throw await buildError(response)
  }

  if (response.status === 204) {
    return null
  }
  return response.json()
}

async function buildError(response) {
  let message = `Request failed (${response.status})`
  try {
    const data = await response.json()
    if (data.fieldErrors && Object.keys(data.fieldErrors).length > 0) {
      message = Object.values(data.fieldErrors).join(' ')
    } else if (data.message) {
      message = data.message
    }
  } catch {
    // Response had no JSON body; keep the generic message.
  }
  const error = new Error(message)
  error.status = response.status
  return error
}

export const auth = {
  login: (username, password) =>
    request('/auth/login', { method: 'POST', body: { username, password } }).then(data => {
      localStorage.setItem(TOKEN_KEY, data.token)
      return data
    }),

  register: (username, password) =>
    request('/auth/register', { method: 'POST', body: { username, password } }),

  logout: () => {
    clearAuth()
  },

  getToken: () => localStorage.getItem(TOKEN_KEY),

  isAuthenticated: () => !!localStorage.getItem(TOKEN_KEY),
}

export const api = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: 'POST', body }),
  put: (path, body) => request(path, { method: 'PUT', body }),
  delete: (path) => request(path, { method: 'DELETE' }),
}
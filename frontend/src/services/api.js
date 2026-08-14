const API_PREFIX = import.meta.env.VITE_API_URL || '/api'
const LOGGED_IN_KEY = 'logged_in'

let csrfToken = null

function setLoggedIn() {
  localStorage.setItem(LOGGED_IN_KEY, '1')
}

function clearLoggedIn() {
  localStorage.removeItem(LOGGED_IN_KEY)
}

function getCsrfToken() {
  return csrfToken
}

async function loadCsrf() {
  const response = await fetch(`${API_PREFIX}/auth/csrf`, {
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Failed to initialize session')
  }

  const data = await response.json()
  csrfToken = data.token
}

async function request(path, { method = 'GET', body, retried = false, ...options } = {}) {
  const csrfToken = getCsrfToken()
  const headers = {
    ...(body ? { 'Content-Type': 'application/json' } : {}),
    ...(csrfToken ? { 'X-XSRF-TOKEN': csrfToken } : {}),
    ...(options.headers || {}),
  }

  const response = await fetch(`${API_PREFIX}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
    credentials: 'include',
    ...options,
  })

  if (response.status === 403 && !retried) {
    await loadCsrf()
    return request(path, { method, body, retried: true, ...options })
  }

  if (response.status === 401 && !path.startsWith('/auth/')) {
    clearLoggedIn()
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
    // response had no JSON body
  }
  const error = new Error(message)
  error.status = response.status
  return error
}

export const auth = {
  init: async () => {
    try {
      await loadCsrf()
    } catch {
      // backend unreachable — requests will fail with a visible error
    }
  },

  login: async (username, password) => {
    const data = await request('/auth/login', { method: 'POST', body: { username, password } })
    setLoggedIn()
    return data
  },

  register: (username, password) =>
    request('/auth/register', { method: 'POST', body: { username, password } }),

  logout: async () => {
    try {
      await request('/auth/logout', { method: 'POST' })
    } catch {
      // server unreachable — cookie may outlive the session, UI state is cleared anyway
    }
    clearLoggedIn()
  },

  me: () => request('/auth/me'),

  isAuthenticated: () => localStorage.getItem(LOGGED_IN_KEY) === '1',
}

export const api = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: 'POST', body }),
  put: (path, body) => request(path, { method: 'PUT', body }),
  delete: (path) => request(path, { method: 'DELETE' }),
}

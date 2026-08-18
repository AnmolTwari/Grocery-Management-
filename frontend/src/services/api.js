const API_PREFIX = '/api'
const LOGGED_IN_KEY = 'logged_in'

let csrfToken = null
let currentUser = null

function setLoggedIn() {
  localStorage.setItem(LOGGED_IN_KEY, '1')
}

function clearLoggedIn() {
  localStorage.removeItem(LOGGED_IN_KEY)
  currentUser = null
}

function applyUser(data) {
  currentUser = data && data.username ? data : null
  return data
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

async function doRequest(path, { method = 'GET', body, retried = false, ...options } = {}) {
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
    return doRequest(path, { method, body, retried: true, ...options })
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

const inFlight = new Map()

const CACHE_TTL_MS = 30 * 1000
const cache = new Map()

function readCache(path) {
  const entry = cache.get(path)
  if (!entry) return undefined
  if (entry.expiresAt <= Date.now()) {
    cache.delete(path)
    return undefined
  }
  return entry.data
}

function invalidateAll() {
  cache.clear()
}

function request(path, options = {}) {
  const { method = 'GET', retried = false } = options
  const key = `${method} ${path}`
  if (method === 'GET' && !retried) {
    const pending = inFlight.get(key)
    if (pending) {
      return pending
    }
    const promise = doRequest(path, options).finally(() => {
      inFlight.delete(key)
    })
    inFlight.set(key, promise)
    return promise
  }
  return doRequest(path, options).then((data) => {
    invalidateAll()
    return data
  })
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
    return applyUser(data)
  },

  register: (username, password, email) =>
    request('/auth/register', { method: 'POST', body: { username, password, email } }),

  logout: async () => {
    try {
      await request('/auth/logout', { method: 'POST' })
    } catch {
      // server unreachable — cookie may outlive the session, UI state is cleared anyway
    }
    clearLoggedIn()
  },

  me: async () => {
    const data = await request('/auth/me')
    return applyUser(data)
  },

  getCurrentUser: () => currentUser,

  isAdmin: () => Boolean(currentUser && currentUser.role === 'ADMIN'),

  isAuthenticated: () => localStorage.getItem(LOGGED_IN_KEY) === '1',
}

export const api = {
  get: (path) => {
    const hit = readCache(path)
    if (hit !== undefined) {
      return Promise.resolve(hit)
    }
    return request(path).then((data) => {
      cache.set(path, { data, expiresAt: Date.now() + CACHE_TTL_MS })
      return data
    })
  },
  post: (path, body) => request(path, { method: 'POST', body }),
  put: (path, body) => request(path, { method: 'PUT', body }),
  patch: (path, body) => request(path, { method: 'PATCH', body }),
  delete: (path) => request(path, { method: 'DELETE' }),
  clearCache: invalidateAll,
}

const API_PREFIX = '/api'

async function request(path, { method = 'GET', body, ...options } = {}) {
  const response = await fetch(`${API_PREFIX}${path}`, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
    ...options,
  })

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

export const api = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: 'POST', body }),
  put: (path, body) => request(path, { method: 'PUT', body }),
  delete: (path) => request(path, { method: 'DELETE' }),
}
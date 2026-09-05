/**
 * 调用后端接口。开发时 Vite 会把 /api 代理到 http://localhost:8080。
 * 后端统一返回 { code, message, data }，code !== 0 视为业务失败。
 */
export async function request(path, options = {}) {
  const response = await fetch(path, {
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
      ...(options.headers || {}),
    },
    ...options,
  })

  let json
  try {
    json = await response.json()
  } catch {
    throw new Error(`HTTP ${response.status}，响应不是 JSON。请确认 Java 后端已启动（8080）。`)
  }

  if (json.code !== 0) {
    throw new Error(json.message || '请求失败')
  }
  return json.data
}

export function getHealth() {
  return request('/api/health')
}

export function listPassengers() {
  return request('/api/passengers')
}

export function createPassenger(body) {
  return request('/api/passengers', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function getPassenger(id) {
  return request(`/api/passengers/${id}`)
}

export function listDrivers() {
  return request('/api/drivers')
}

export function createDriver(body) {
  return request('/api/drivers', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function getDriver(id) {
  return request(`/api/drivers/${id}`)
}

export function listOrders(status) {
  const query = status ? `?status=${encodeURIComponent(status)}` : ''
  return request(`/api/orders${query}`)
}

export function getOrder(id) {
  return request(`/api/orders/${id}`)
}

export function createOrder(body) {
  return request('/api/orders', { method: 'POST', body: JSON.stringify(body) })
}

export function postOrderAction(id, action, body) {
  return request(`/api/orders/${id}/${action}`, {
    method: 'POST',
    body: JSON.stringify(body || {}),
  })
}

export function getFare(id) {
  return request(`/api/orders/${id}/fare`)
}

export function listPayments() {
  return request('/api/payments')
}

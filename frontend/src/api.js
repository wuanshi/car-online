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

export function listDrivers() {
  return request('/api/drivers')
}

export function createDriver(body) {
  return request('/api/drivers', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

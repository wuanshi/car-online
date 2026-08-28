import { useEffect, useState } from 'react'
import { getHealth } from '../api.js'

export default function Overview() {
  const [health, setHealth] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  async function load() {
    setLoading(true)
    setError('')
    try {
      setHealth(await getHealth())
    } catch (e) {
      setHealth(null)
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const dbUp = health?.db === 'UP'

  return (
    <section>
      <header className="page-head">
        <div>
          <h2>服务概览</h2>
          <p>对应接口 GET /api/health</p>
        </div>
        <button type="button" onClick={load} disabled={loading}>
          {loading ? '检查中…' : '重新检查'}
        </button>
      </header>

      {error && <p className="banner error">{error}</p>}

      <div className="cards">
        <article className="card">
          <span className="label">应用</span>
          <strong>{health?.app || '—'}</strong>
        </article>
        <article className="card">
          <span className="label">HTTP</span>
          <strong className={health ? 'ok' : 'bad'}>{health ? health.status : 'DOWN'}</strong>
        </article>
        <article className="card">
          <span className="label">MySQL</span>
          <strong className={dbUp ? 'ok' : 'bad'}>{health?.db || 'DOWN'}</strong>
        </article>
      </div>

      <p className="hint">
        前端跑在 5173，Java 后端跑在 8080。开发时 Vite 会把 <code>/api</code> 转发到后端。
      </p>
    </section>
  )
}

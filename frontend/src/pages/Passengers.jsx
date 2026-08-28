import { useEffect, useState } from 'react'
import { createPassenger, listPassengers } from '../api.js'

const empty = { name: '', phone: '' }

export default function Passengers() {
  const [rows, setRows] = useState([])
  const [form, setForm] = useState(empty)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function load() {
    setError('')
    try {
      setRows(await listPassengers())
    } catch (e) {
      setError(e.message)
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function onSubmit(event) {
    event.preventDefault()
    setSaving(true)
    setMessage('')
    setError('')
    try {
      await createPassenger({ name: form.name.trim(), phone: form.phone.trim() })
      setForm(empty)
      setMessage('乘客已创建')
      await load()
    } catch (e) {
      setError(e.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <section>
      <header className="page-head">
        <div>
          <h2>乘客</h2>
          <p>GET / POST /api/passengers</p>
        </div>
      </header>

      <form className="panel form" onSubmit={onSubmit}>
        <h3>新增乘客</h3>
        <label>
          姓名
          <input
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            placeholder="张三"
            required
          />
        </label>
        <label>
          手机号
          <input
            value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })}
            placeholder="13800000000"
            required
          />
        </label>
        <button type="submit" disabled={saving}>
          {saving ? '提交中…' : '创建'}
        </button>
      </form>

      {message && <p className="banner ok">{message}</p>}
      {error && <p className="banner error">{error}</p>}

      <div className="panel">
        <h3>乘客列表（{rows.length}）</h3>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>姓名</th>
              <th>手机号</th>
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 ? (
              <tr>
                <td colSpan={3} className="empty">暂无数据</td>
              </tr>
            ) : (
              rows.map((row) => (
                <tr key={row.id}>
                  <td>{row.id}</td>
                  <td>{row.name}</td>
                  <td>{row.phone}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}

import { useEffect, useState } from 'react'
import { createDriver, listDrivers } from '../api.js'

const empty = { name: '', phone: '', plate: '', brand: '', color: '' }

export default function Drivers() {
  const [rows, setRows] = useState([])
  const [form, setForm] = useState(empty)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function load() {
    setError('')
    try {
      setRows(await listDrivers())
    } catch (e) {
      setError(e.message)
    }
  }

  useEffect(() => {
    load()
  }, [])

  function setField(key, value) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function onSubmit(event) {
    event.preventDefault()
    setSaving(true)
    setMessage('')
    setError('')
    try {
      await createDriver({
        name: form.name.trim(),
        phone: form.phone.trim(),
        plate: form.plate.trim(),
        brand: form.brand.trim(),
        color: form.color.trim(),
      })
      setForm(empty)
      setMessage('司机已注册')
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
          <h2>司机</h2>
          <p>GET / POST /api/drivers，注册时必须填写车牌</p>
        </div>
      </header>

      <form className="panel form" onSubmit={onSubmit}>
        <h3>注册司机并绑车</h3>
        <div className="grid">
          <label>
            姓名
            <input value={form.name} onChange={(e) => setField('name', e.target.value)} placeholder="李四" required />
          </label>
          <label>
            手机号
            <input value={form.phone} onChange={(e) => setField('phone', e.target.value)} placeholder="13900000000" required />
          </label>
          <label>
            车牌
            <input value={form.plate} onChange={(e) => setField('plate', e.target.value)} placeholder="粤A12345" required />
          </label>
          <label>
            品牌
            <input value={form.brand} onChange={(e) => setField('brand', e.target.value)} placeholder="比亚迪" />
          </label>
          <label>
            颜色
            <input value={form.color} onChange={(e) => setField('color', e.target.value)} placeholder="白" />
          </label>
        </div>
        <button type="submit" disabled={saving}>
          {saving ? '提交中…' : '注册'}
        </button>
      </form>

      {message && <p className="banner ok">{message}</p>}
      {error && <p className="banner error">{error}</p>}

      <div className="panel">
        <h3>司机列表（{rows.length}）</h3>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>姓名</th>
              <th>手机号</th>
              <th>车牌</th>
              <th>品牌</th>
              <th>颜色</th>
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 ? (
              <tr>
                <td colSpan={6} className="empty">暂无数据</td>
              </tr>
            ) : (
              rows.map((row) => (
                <tr key={row.id}>
                  <td>{row.id}</td>
                  <td>{row.name}</td>
                  <td>{row.phone}</td>
                  <td>{row.vehicle?.plate}</td>
                  <td>{row.vehicle?.brand || '—'}</td>
                  <td>{row.vehicle?.color || '—'}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}

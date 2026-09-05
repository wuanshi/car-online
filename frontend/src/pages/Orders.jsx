import { useEffect, useState } from 'react'
import { createOrder, getFare, listOrders, listPayments, postOrderAction } from '../api.js'

const STATUSES = ['', 'WAITING_ACCEPT', 'ACCEPTED', 'DRIVER_ARRIVED', 'IN_TRIP', 'COMPLETED', 'CANCELLED']

const emptyOrder = { passengerId: '', origin: '家', destination: '公司', distanceKm: '5' }

export default function Orders() {
  const [rows, setRows] = useState([])
  const [payments, setPayments] = useState([])
  const [filter, setFilter] = useState('')
  const [form, setForm] = useState(emptyOrder)
  const [driverId, setDriverId] = useState('')
  const [stars, setStars] = useState('5')
  const [comment, setComment] = useState('')
  const [fare, setFare] = useState(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [selected, setSelected] = useState(null)

  async function load() {
    setError('')
    try {
      const [orderRows, paymentRows] = await Promise.all([listOrders(filter), listPayments()])
      setRows(orderRows)
      setPayments(paymentRows)
    } catch (e) {
      setError(e.message)
    }
  }

  useEffect(() => {
    load()
  }, [filter])

  async function run(task, okText) {
    setError('')
    setMessage('')
    try {
      const result = await task()
      setMessage(okText)
      if (result && result.id) {
        setSelected(result)
      }
      await load()
      return result
    } catch (e) {
      setError(e.message)
      return null
    }
  }

  return (
    <section>
      <header className="page-head">
        <div>
          <h2>行程订单</h2>
          <p>发单 → 接单 → 到达 → 开始 → 结束 → 支付 → 评价</p>
        </div>
      </header>

      <form
        className="panel form"
        onSubmit={(event) => {
          event.preventDefault()
          run(
            () => createOrder({
              passengerId: Number(form.passengerId),
              origin: form.origin,
              destination: form.destination,
              distanceKm: Number(form.distanceKm),
            }),
            '已发单',
          )
        }}
      >
        <h3>乘客发单</h3>
        <div className="grid">
          <label>
            乘客 ID
            <input value={form.passengerId} onChange={(e) => setForm({ ...form, passengerId: e.target.value })} required />
          </label>
          <label>
            公里数
            <input value={form.distanceKm} onChange={(e) => setForm({ ...form, distanceKm: e.target.value })} required />
          </label>
          <label>
            起点
            <input value={form.origin} onChange={(e) => setForm({ ...form, origin: e.target.value })} required />
          </label>
          <label>
            终点
            <input value={form.destination} onChange={(e) => setForm({ ...form, destination: e.target.value })} required />
          </label>
        </div>
        <button type="submit">发单</button>
      </form>

      <div className="panel form">
        <h3>操作选中订单 #{selected?.id || '—'}</h3>
        <p className="hint">状态：{selected?.status || '先在下表点一行'}</p>
        <div className="grid">
          <label>
            司机 ID（接单用）
            <input value={driverId} onChange={(e) => setDriverId(e.target.value)} />
          </label>
          <label>
            星级
            <input value={stars} onChange={(e) => setStars(e.target.value)} />
          </label>
        </div>
        <label>
          评价
          <input value={comment} onChange={(e) => setComment(e.target.value)} placeholder="可选" />
        </label>
        <div className="actions">
          <button type="button" disabled={!selected} onClick={() => run(() => postOrderAction(selected.id, 'cancel'), '已取消')}>取消</button>
          <button type="button" disabled={!selected} onClick={() => run(() => postOrderAction(selected.id, 'accept', { driverId: Number(driverId) }), '已接单')}>接单</button>
          <button type="button" disabled={!selected} onClick={() => run(() => postOrderAction(selected.id, 'arrive'), '已到达')}>到达</button>
          <button type="button" disabled={!selected} onClick={() => run(() => postOrderAction(selected.id, 'start'), '行程开始')}>开始</button>
          <button type="button" disabled={!selected} onClick={() => run(() => postOrderAction(selected.id, 'finish'), '行程结束')}>结束</button>
          <button type="button" disabled={!selected} onClick={async () => setFare(await getFare(selected.id))}>看费用</button>
          <button type="button" disabled={!selected} onClick={() => run(() => postOrderAction(selected.id, 'pay'), '已支付')}>支付</button>
          <button type="button" disabled={!selected} onClick={() => run(() => postOrderAction(selected.id, 'rating', { stars: Number(stars), comment }), '已评价')}>评价</button>
        </div>
        {fare && <p className="hint">费用：起步 {fare.startPrice} + {fare.perKm}/公里 × {fare.distanceKm} = {fare.fare}</p>}
      </div>

      {message && <p className="banner ok">{message}</p>}
      {error && <p className="banner error">{error}</p>}

      <div className="panel">
        <h3>订单列表</h3>
        <label>
          状态筛选
          <select value={filter} onChange={(e) => setFilter(e.target.value)}>
            {STATUSES.map((item) => (
              <option key={item || 'all'} value={item}>{item || '全部'}</option>
            ))}
          </select>
        </label>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>乘客</th>
              <th>司机</th>
              <th>行程</th>
              <th>状态</th>
              <th>费用</th>
              <th>支付</th>
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 ? (
              <tr><td colSpan={7} className="empty">暂无订单</td></tr>
            ) : rows.map((row) => (
              <tr key={row.id} className={selected?.id === row.id ? 'picked' : ''} onClick={() => { setSelected(row); setFare(null) }}>
                <td>{row.id}</td>
                <td>{row.passengerName} #{row.passengerId}</td>
                <td>{row.driverName ? `${row.driverName} ${row.plate || ''}` : '—'}</td>
                <td>{row.origin} → {row.destination}（{row.distanceKm}km）</td>
                <td>{row.status}</td>
                <td>{row.fare ?? '—'}</td>
                <td>{row.paid ? '已付' : '未付'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="panel">
        <h3>支付流水（{payments.length}）</h3>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>订单</th>
              <th>金额</th>
            </tr>
          </thead>
          <tbody>
            {payments.length === 0 ? (
              <tr><td colSpan={3} className="empty">暂无流水</td></tr>
            ) : payments.map((row) => (
              <tr key={row.id}>
                <td>{row.id}</td>
                <td>#{row.orderId}</td>
                <td>{row.amount}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}

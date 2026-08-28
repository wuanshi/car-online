import { useState } from 'react'
import Overview from './pages/Overview.jsx'
import Passengers from './pages/Passengers.jsx'
import Drivers from './pages/Drivers.jsx'

const TABS = [
  { id: 'overview', label: '概览' },
  { id: 'passengers', label: '乘客' },
  { id: 'drivers', label: '司机' },
]

export default function App() {
  const [tab, setTab] = useState('overview')

  return (
    <div className="shell">
      <aside>
        <h1>car-online</h1>
        <p className="sub">打车学习项目 · 管理台</p>
        <nav>
          {TABS.map((item) => (
            <button
              key={item.id}
              type="button"
              className={tab === item.id ? 'active' : ''}
              onClick={() => setTab(item.id)}
            >
              {item.label}
            </button>
          ))}
        </nav>
      </aside>
      <main>
        {tab === 'overview' && <Overview />}
        {tab === 'passengers' && <Passengers />}
        {tab === 'drivers' && <Drivers />}
      </main>
    </div>
  )
}

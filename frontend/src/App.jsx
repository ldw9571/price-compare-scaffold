import React from 'react'
import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom'
import ProductListPage from './pages/ProductListPage.jsx'
import ProductDetailPage from './pages/ProductDetailPage.jsx'
import ScanLogPage from './pages/ScanLogPage.jsx'
import KeywordSettingsPage from './pages/KeywordSettingsPage.jsx'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <div className="app-shell">
        <header className="app-header">
          <h1>위탁판매 가격비교 대시보드</h1>
          <nav className="app-nav">
            <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
              상품 리스트
            </NavLink>
            <NavLink to="/scan-logs" className={({ isActive }) => (isActive ? 'active' : '')}>
              스캔 이력
            </NavLink>
            <NavLink to="/keywords" className={({ isActive }) => (isActive ? 'active' : '')}>
              감시 키워드 관리
            </NavLink>
          </nav>
        </header>
        <main className="app-main">
          <Routes>
            <Route path="/" element={<ProductListPage />} />
            <Route path="/products/:id" element={<ProductDetailPage />} />
            <Route path="/scan-logs" element={<ScanLogPage />} />
            <Route path="/keywords" element={<KeywordSettingsPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}

export default App

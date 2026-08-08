import React, { useEffect, useState, useCallback } from 'react'
import apiClient from '../api/client.js'
import ProductTable from '../components/ProductTable.jsx'
import FilterBar from '../components/FilterBar.jsx'

function ProductListPage() {
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [filters, setFilters] = useState({
    category: '',
    sort: 'score',
    minMarginPercent: '',
    moqOneOnly: false,
  })

  const fetchProducts = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const params = {
        sort: filters.sort,
        moqOneOnly: filters.moqOneOnly,
      }
      if (filters.category) params.category = filters.category
      if (filters.minMarginPercent !== '') {
        params.minMargin = Number(filters.minMarginPercent) / 100
      }
      const res = await apiClient.get('/products', { params })
      setProducts(res.data)
    } catch (e) {
      setError('상품 목록을 불러오지 못했습니다. 백엔드 서버가 실행 중인지 확인해주세요.')
    } finally {
      setLoading(false)
    }
  }, [filters])

  useEffect(() => {
    fetchProducts()
  }, [fetchProducts])

  useEffect(() => {
    apiClient.get('/products/categories').then((res) => setCategories(res.data)).catch(() => {})
  }, [])

  return (
    <div>
      <div className="card">
        <FilterBar categories={categories} filters={filters} onChange={setFilters} />
        {loading && <p>불러오는 중...</p>}
        {error && <p style={{ color: '#c23b3b' }}>{error}</p>}
        {!loading && !error && <ProductTable products={products} />}
      </div>
    </div>
  )
}

export default ProductListPage

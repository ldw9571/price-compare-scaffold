import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import apiClient from '../api/client.js'
import PriceTrendChart from '../components/PriceTrendChart.jsx'

function formatPrice(value) {
  if (value === null || value === undefined) return '-'
  return Number(value).toLocaleString('ko-KR') + '원'
}

function ProductDetailPage() {
  const { id } = useParams()
  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    apiClient.get(`/products/${id}`)
      .then((res) => setProduct(res.data))
      .catch(() => setError('상품 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <p>불러오는 중...</p>
  if (error) return <p style={{ color: '#c23b3b' }}>{error}</p>
  if (!product) return null

  return (
    <div>
      <Link to="/" className="link-btn">&larr; 목록으로</Link>

      <div className="card" style={{ marginTop: 12 }}>
        <h2>{product.itemName}</h2>
        <p>카테고리: {product.category || '-'}</p>
        <p>도매가: {formatPrice(product.wholesalePrice)} / 배송비: {formatPrice(product.shippingFee)}</p>
        <p>MOQ: {product.moq === 1 ? '1개 (위탁 가능)' : product.moq}</p>
        {product.itemUrl && (
          <p><a href={product.itemUrl} target="_blank" rel="noreferrer">도매꾹 원본 링크</a></p>
        )}
      </div>

      <div className="card">
        <h3>가격 추이</h3>
        <PriceTrendChart history={product.priceHistory} />
      </div>

      <div className="card">
        <h3>스캔 이력 상세</h3>
        <table>
          <thead>
            <tr>
              <th>스캔 시각</th>
              <th>네이버 최저가</th>
              <th>경쟁력 점수</th>
              <th>마진율</th>
              <th>네이버 링크</th>
            </tr>
          </thead>
          <tbody>
            {product.priceHistory.map((h, idx) => (
              <tr key={idx}>
                <td>{new Date(h.scannedAt).toLocaleString('ko-KR')}</td>
                <td>{formatPrice(h.naverLowestPrice)}</td>
                <td>{(h.competitivenessScore * 100).toFixed(1)}%</td>
                <td>{(h.marginRate * 100).toFixed(1)}%</td>
                <td>{h.naverProductUrl && <a href={h.naverProductUrl} target="_blank" rel="noreferrer">보기</a>}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default ProductDetailPage

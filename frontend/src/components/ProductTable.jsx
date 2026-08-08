import React from 'react'
import { useNavigate } from 'react-router-dom'

function formatPrice(value) {
  if (value === null || value === undefined) return '-'
  return Number(value).toLocaleString('ko-KR') + '원'
}

function formatPercent(value) {
  if (value === null || value === undefined) return '-'
  return (Number(value) * 100).toFixed(1) + '%'
}

function ProductTable({ products }) {
  const navigate = useNavigate()

  if (!products || products.length === 0) {
    return <div className="empty-state">표시할 상품이 없습니다. 아직 스캔이 실행되지 않았을 수 있습니다.</div>
  }

  return (
    <table>
      <thead>
        <tr>
          <th>상품명</th>
          <th>카테고리</th>
          <th>도매가</th>
          <th>배송비</th>
          <th>네이버 최저가</th>
          <th>경쟁력 점수</th>
          <th>마진율</th>
          <th>MOQ</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {products.map((p) => (
          <tr key={p.id} onClick={() => navigate(`/products/${p.id}`)} style={{ cursor: 'pointer' }}>
            <td>{p.itemName}</td>
            <td>{p.category || '-'}</td>
            <td>{formatPrice(p.wholesalePrice)}</td>
            <td>{formatPrice(p.shippingFee)}</td>
            <td>{formatPrice(p.naverLowestPrice)}</td>
            <td className={p.competitivenessScore >= 0 ? 'score-positive' : 'score-negative'}>
              {formatPercent(p.competitivenessScore)}
            </td>
            <td>{formatPercent(p.marginRate)}</td>
            <td>{p.moq === 1 ? '가능' : (p.moq ?? '-')}</td>
            <td>
              {p.itemUrl && (
                <a href={p.itemUrl} target="_blank" rel="noreferrer" onClick={(e) => e.stopPropagation()}>
                  원본
                </a>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}

export default ProductTable

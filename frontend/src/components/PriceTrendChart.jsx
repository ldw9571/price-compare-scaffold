import React from 'react'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'

function PriceTrendChart({ history }) {
  if (!history || history.length === 0) {
    return <div className="empty-state">가격 추이 데이터가 아직 없습니다.</div>
  }

  // 차트는 시간순(오래된 것부터)으로 표시
  const data = [...history].reverse().map((h) => ({
    date: new Date(h.scannedAt).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' }),
    naverLowestPrice: Number(h.naverLowestPrice),
  }))

  return (
    <ResponsiveContainer width="100%" height={260}>
      <LineChart data={data}>
        <CartesianGrid strokeDasharray="3 3" stroke="#eee" />
        <XAxis dataKey="date" fontSize={12} />
        <YAxis fontSize={12} tickFormatter={(v) => v.toLocaleString('ko-KR')} />
        <Tooltip formatter={(v) => v.toLocaleString('ko-KR') + '원'} />
        <Line type="monotone" dataKey="naverLowestPrice" name="네이버 최저가" stroke="#3a5bbf" strokeWidth={2} dot={{ r: 3 }} />
      </LineChart>
    </ResponsiveContainer>
  )
}

export default PriceTrendChart

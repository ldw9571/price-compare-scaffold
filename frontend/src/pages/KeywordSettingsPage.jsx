import React, { useEffect, useState, useCallback } from 'react'
import apiClient from '../api/client.js'

function KeywordSettingsPage() {
  const [keywords, setKeywords] = useState([])
  const [loading, setLoading] = useState(true)
  const [newKeyword, setNewKeyword] = useState('')
  const [newCategory, setNewCategory] = useState('')
  const [newPriority, setNewPriority] = useState('')

  const fetchKeywords = useCallback(async () => {
    setLoading(true)
    try {
      const res = await apiClient.get('/keywords')
      setKeywords(res.data)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchKeywords()
  }, [fetchKeywords])

  const handleAdd = async (e) => {
    e.preventDefault()
    if (!newKeyword.trim()) return
    try {
      await apiClient.post('/keywords', {
        keyword: newKeyword.trim(),
        category: newCategory.trim() || null,
        priority: newPriority ? Number(newPriority) : null,
      })
      setNewKeyword('')
      setNewCategory('')
      setNewPriority('')
      fetchKeywords()
    } catch (e) {
      alert('키워드 등록에 실패했습니다. 이미 등록된 키워드일 수 있습니다.')
    }
  }

  const handleToggleActive = async (keyword) => {
    await apiClient.put(`/keywords/${keyword.id}`, { active: !keyword.active })
    fetchKeywords()
  }

  const handleDelete = async (id) => {
    if (!confirm('이 키워드를 삭제할까요?')) return
    await apiClient.delete(`/keywords/${id}`)
    fetchKeywords()
  }

  return (
    <div className="card">
      <h2>감시 키워드 관리</h2>
      <p style={{ color: '#666', fontSize: 13 }}>
        여기 등록된 활성(active) 키워드를 기준으로 매일 새벽 배치가 도매꾹 상품을 수집합니다.
      </p>

      <form onSubmit={handleAdd} style={{ display: 'flex', gap: 8, margin: '16px 0' }}>
        <input placeholder="키워드 (예: 음식물처리기)" value={newKeyword} onChange={(e) => setNewKeyword(e.target.value)} />
        <input placeholder="카테고리 (선택)" value={newCategory} onChange={(e) => setNewCategory(e.target.value)} />
        <input placeholder="우선순위 (선택, 낮을수록 높음)" type="number" value={newPriority} onChange={(e) => setNewPriority(e.target.value)} style={{ width: 180 }} />
        <button className="btn" type="submit">추가</button>
      </form>

      {loading ? (
        <p>불러오는 중...</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>키워드</th>
              <th>카테고리</th>
              <th>우선순위</th>
              <th>활성 여부</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {keywords.map((k) => (
              <tr key={k.id}>
                <td>{k.keyword}</td>
                <td>{k.category || '-'}</td>
                <td>{k.priority ?? '-'}</td>
                <td>
                  <button className="link-btn" onClick={() => handleToggleActive(k)}>
                    {k.active ? '✅ 활성' : '⬜ 비활성'}
                  </button>
                </td>
                <td>
                  <button className="link-btn" style={{ color: '#c23b3b' }} onClick={() => handleDelete(k.id)}>
                    삭제
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}

export default KeywordSettingsPage

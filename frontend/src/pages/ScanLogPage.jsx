import React, { useEffect, useState, useCallback } from 'react'
import apiClient from '../api/client.js'

function ScanLogPage() {
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(true)
  const [running, setRunning] = useState(false)

  const fetchLogs = useCallback(async () => {
    setLoading(true)
    try {
      const res = await apiClient.get('/scan-logs')
      setLogs(res.data)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchLogs()
  }, [fetchLogs])

  const handleRunNow = async () => {
    setRunning(true)
    try {
      await apiClient.post('/scan-logs/run')
      await fetchLogs()
    } catch (e) {
      alert('스캔 실행에 실패했습니다.')
    } finally {
      setRunning(false)
    }
  }

  return (
    <div className="card">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>스캔 이력</h2>
        <button className="btn" onClick={handleRunNow} disabled={running}>
          {running ? '실행 중... (시간이 걸릴 수 있어요)' : '지금 스캔 실행'}
        </button>
      </div>

      {loading ? (
        <p>불러오는 중...</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>구분</th>
              <th>시작 시각</th>
              <th>종료 시각</th>
              <th>상태</th>
              <th>수집 건수</th>
              <th>에러</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((log) => (
              <tr key={log.id}>
                <td>{log.scanType}</td>
                <td>{new Date(log.startedAt).toLocaleString('ko-KR')}</td>
                <td>{log.finishedAt ? new Date(log.finishedAt).toLocaleString('ko-KR') : '-'}</td>
                <td><span className={`status-badge status-${log.status}`}>{log.status}</span></td>
                <td>{log.itemsCollected ?? '-'}</td>
                <td style={{ color: '#c23b3b', fontSize: 12 }}>{log.errorMessage || ''}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}

export default ScanLogPage

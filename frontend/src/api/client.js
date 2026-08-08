import axios from 'axios'

// 개발 환경에서는 vite.config.js의 proxy 설정을 통해 /api가 백엔드로 전달됩니다.
const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

export default apiClient

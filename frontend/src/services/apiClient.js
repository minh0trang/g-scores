import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error.response?.status || 0
    const serverMessage = error.response?.data?.message

    let message =
      serverMessage || 'Đã xảy ra lỗi kết nối đến máy chủ. Vui lòng thử lại.'

    if (status === 400) {
      message =
        serverMessage ||
        'Số báo danh không hợp lệ. Vui lòng nhập đúng 8 chữ số (ví dụ: 01000001).'
    } else if (status === 404) {
      message =
        serverMessage ||
        'Không tìm thấy thí sinh với Số báo danh này trong hệ thống.'
    }

    const normalizedError = new Error(message)
    normalizedError.status = status
    normalizedError.isNetworkError = !error.response

    return Promise.reject(normalizedError)
  },
)

export default apiClient


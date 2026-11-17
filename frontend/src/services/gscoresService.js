import apiClient from './apiClient'
import {
  MOCK_OVERVIEW_STATS,
  MOCK_SCORE_DISTRIBUTION,
  MOCK_STUDENTS_BY_SBD,
  MOCK_TOP_10_GROUP_A,
} from './mockData'

const USE_MOCK_BY_DEFAULT = import.meta.env.VITE_USE_MOCK_API === 'true'
const MOCK_DELAY_MS = 250

function simulateDelay(ms = MOCK_DELAY_MS) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

export function isValidSbdFormat(sbd) {
  return typeof sbd === 'string' && /^\d{8}$/.test(sbd.trim())
}

function getMockStudentBySbd(rawSbd) {
  const normalized = String(rawSbd ?? '').trim()

  if (!isValidSbdFormat(normalized)) {
    const err = new Error(
      'Số báo danh không hợp lệ. Vui lòng nhập đúng 8 chữ số (ví dụ: 01000001).',
    )
    err.status = 400
    throw err
  }

  if (MOCK_STUDENTS_BY_SBD[normalized]) {
    return MOCK_STUDENTS_BY_SBD[normalized]
  }

  // Hỗ trợ tra cứu cả các thí sinh nằm trong bảng Top 10 Khối A
  const topCandidate = MOCK_TOP_10_GROUP_A.find(
    (item) => item.sbd === normalized,
  )
  if (topCandidate) {
    return {
      sbd: topCandidate.sbd,
      languageCode: topCandidate.languageCode,
      scores: {
        TOAN: topCandidate.toan,
        NGU_VAN: 7.75,
        NGOAI_NGU: topCandidate.languageCode ? 8.6 : null,
        VAT_LI: topCandidate.vatLi,
        HOA_HOC: topCandidate.hoaHoc,
        SINH_HOC: 7.5,
        LICH_SU: null,
        DIA_LI: null,
        GDCD: null,
      },
    }
  }

  const notFoundError = new Error(
    `Không tìm thấy dữ liệu điểm thi cho Số báo danh "${normalized}".`,
  )
  notFoundError.status = 404
  throw notFoundError
}

/**
 * GET /api/v1/students/{sbd}/scores
 */
export async function fetchStudentScoresBySbd(sbd) {
  const normalized = String(sbd ?? '').trim()

  if (!isValidSbdFormat(normalized)) {
    const err = new Error(
      'Số báo danh không hợp lệ. Vui lòng nhập đúng 8 chữ số (ví dụ: 01000001).',
    )
    err.status = 400
    throw err
  }

  if (USE_MOCK_BY_DEFAULT) {
    await simulateDelay()
    return getMockStudentBySbd(normalized)
  }

  try {
    return await apiClient.get(`/students/${encodeURIComponent(normalized)}/scores`)
  } catch (error) {
    if (error.isNetworkError) {
      await simulateDelay(150)
      return getMockStudentBySbd(normalized)
    }
    throw error
  }
}

/**
 * GET /api/v1/reports/overview
 */
export async function fetchOverviewStats() {
  if (USE_MOCK_BY_DEFAULT) {
    await simulateDelay(200)
    return MOCK_OVERVIEW_STATS
  }

  try {
    return await apiClient.get('/reports/overview')
  } catch (error) {
    if (error.isNetworkError) {
      return MOCK_OVERVIEW_STATS
    }
    throw error
  }
}

/**
 * GET /api/v1/reports/score-distribution
 */
export async function fetchScoreDistribution() {
  if (USE_MOCK_BY_DEFAULT) {
    await simulateDelay(250)
    return MOCK_SCORE_DISTRIBUTION
  }

  try {
    return await apiClient.get('/reports/score-distribution')
  } catch (error) {
    if (error.isNetworkError) {
      return MOCK_SCORE_DISTRIBUTION
    }
    throw error
  }
}

/**
 * GET /api/v1/reports/group-a/top-10
 */
export async function fetchTop10GroupA() {
  if (USE_MOCK_BY_DEFAULT) {
    await simulateDelay(250)
    return MOCK_TOP_10_GROUP_A
  }

  try {
    return await apiClient.get('/reports/group-a/top-10')
  } catch (error) {
    if (error.isNetworkError) {
      return MOCK_TOP_10_GROUP_A
    }
    throw error
  }
}


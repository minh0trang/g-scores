import apiClient from './apiClient'
import {
  MOCK_OVERVIEW_STATS,
  MOCK_PROVINCES,
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

  const topCandidate = MOCK_TOP_10_GROUP_A.find(
    (item) => item.sbd === normalized,
  )
  if (topCandidate) {
    return {
      sbd: topCandidate.sbd,
      provinceCode: topCandidate.provinceCode || '01',
      provinceName: topCandidate.provinceName || 'Hà Nội',
      languageCode: topCandidate.languageCode,
      languageDisplayName: topCandidate.languageCode ? 'Ngoại ngữ (N1 - Tiếng Anh)' : null,
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
      blocks: [
        {
          blockCode: 'A00',
          blockName: 'Toán, Vật lí, Hóa học',
          totalScore: topCandidate.totalScore,
          nationalRank: topCandidate.rank,
          nationalTotal: 343800,
          provincialRank: 1,
          provincialTotal: 29149,
        },
      ],
    }
  }

  const notFoundError = new Error(
    `Không tìm thấy dữ liệu điểm thi cho Số báo danh "${normalized}".`,
  )
  notFoundError.status = 404
  throw notFoundError
}

/**
 * GET /api/v1/provinces
 */
export async function fetchProvinces() {
  if (USE_MOCK_BY_DEFAULT) {
    await simulateDelay(150)
    return MOCK_PROVINCES
  }

  return apiClient.get('/provinces')
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

  return apiClient.get(`/students/${encodeURIComponent(normalized)}/scores`)
}

/**
 * GET /api/v1/reports/overview(?province=<code>)
 */
export async function fetchOverviewStats(province) {
  if (USE_MOCK_BY_DEFAULT) {
    await simulateDelay(200)
    return MOCK_OVERVIEW_STATS
  }

  const params = province ? { province } : {}
  return apiClient.get('/reports/overview', { params })
}

/**
 * GET /api/v1/reports/score-distribution(?province=<code>)
 */
export async function fetchScoreDistribution(province) {
  if (USE_MOCK_BY_DEFAULT) {
    await simulateDelay(250)
    return MOCK_SCORE_DISTRIBUTION
  }

  const params = province ? { province } : {}
  return apiClient.get('/reports/score-distribution', { params })
}

/**
 * GET /api/v1/reports/group-a/top-10(?province=<code>)
 */
export async function fetchTop10GroupA(province) {
  if (USE_MOCK_BY_DEFAULT) {
    await simulateDelay(250)
    return MOCK_TOP_10_GROUP_A
  }

  const params = province ? { province } : {}
  return apiClient.get('/reports/group-a/top-10', { params })
}

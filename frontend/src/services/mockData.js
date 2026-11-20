export const MOCK_PROVINCES = [
  { code: '01', name: 'Hà Nội' },
  { code: '02', name: 'TP. Hồ Chí Minh' },
  { code: '03', name: 'Hải Phòng' },
  { code: '04', name: 'Đà Nẵng' },
  { code: '05', name: 'Hà Giang' },
  { code: '06', name: 'Cao Bằng' },
  { code: '07', name: 'Lai Châu' },
  { code: '08', name: 'Lào Cai' },
  { code: '09', name: 'Tuyên Quang' },
  { code: '10', name: 'Lạng Sơn' },
  { code: '11', name: 'Bắc Kạn' },
  { code: '12', name: 'Thái Nguyên' },
  { code: '13', name: 'Yên Bái' },
  { code: '14', name: 'Sơn La' },
  { code: '15', name: 'Phú Thọ' },
  { code: '16', name: 'Vĩnh Phúc' },
  { code: '17', name: 'Quảng Ninh' },
  { code: '18', name: 'Bắc Giang' },
  { code: '19', name: 'Bắc Ninh' },
  { code: '21', name: 'Hải Dương' },
  { code: '22', name: 'Hưng Yên' },
  { code: '23', name: 'Hòa Bình' },
  { code: '24', name: 'Hà Nam' },
  { code: '25', name: 'Nam Định' },
  { code: '26', name: 'Thái Bình' },
  { code: '27', name: 'Ninh Bình' },
  { code: '28', name: 'Thanh Hóa' },
  { code: '29', name: 'Nghệ An' },
  { code: '30', name: 'Hà Tĩnh' },
  { code: '31', name: 'Quảng Bình' },
  { code: '32', name: 'Quảng Trị' },
  { code: '33', name: 'Thừa Thiên Huế' },
  { code: '34', name: 'Quảng Nam' },
  { code: '35', name: 'Quảng Ngãi' },
  { code: '36', name: 'Kon Tum' },
  { code: '37', name: 'Bình Định' },
  { code: '38', name: 'Gia Lai' },
  { code: '39', name: 'Phú Yên' },
  { code: '40', name: 'Đắk Lắk' },
  { code: '41', name: 'Khánh Hòa' },
  { code: '42', name: 'Lâm Đồng' },
  { code: '43', name: 'Bình Phước' },
  { code: '44', name: 'Bình Dương' },
  { code: '45', name: 'Ninh Thuận' },
  { code: '46', name: 'Tây Ninh' },
  { code: '47', name: 'Bình Thuận' },
  { code: '48', name: 'Đồng Nai' },
  { code: '49', name: 'Long An' },
  { code: '50', name: 'Đồng Tháp' },
  { code: '51', name: 'An Giang' },
  { code: '52', name: 'Bà Rịa - Vũng Tàu' },
  { code: '53', name: 'Tiền Giang' },
  { code: '54', name: 'Kiên Giang' },
  { code: '55', name: 'Cần Thơ' },
  { code: '56', name: 'Bến Tre' },
  { code: '57', name: 'Vĩnh Long' },
  { code: '58', name: 'Trà Vinh' },
  { code: '59', name: 'Sóc Trăng' },
  { code: '60', name: 'Bạc Liêu' },
  { code: '61', name: 'Cà Mau' },
  { code: '62', name: 'Điện Biên' },
  { code: '63', name: 'Đắk Nông' },
  { code: '64', name: 'Hậu Giang' },
]

export const SUBJECTS = [
  { code: 'TOAN', name: 'Toán', category: 'Bắt buộc' },
  { code: 'NGU_VAN', name: 'Ngữ văn', category: 'Bắt buộc' },
  { code: 'NGOAI_NGU', name: 'Ngoại ngữ', category: 'Bắt buộc' },
  { code: 'VAT_LI', name: 'Vật lí', category: 'Khoa học Tự nhiên' },
  { code: 'HOA_HOC', name: 'Hóa học', category: 'Khoa học Tự nhiên' },
  { code: 'SINH_HOC', name: 'Sinh học', category: 'Khoa học Tự nhiên' },
  { code: 'LICH_SU', name: 'Lịch sử', category: 'Khoa học Xã hội' },
  { code: 'DIA_LI', name: 'Địa lí', category: 'Khoa học Xã hội' },
  { code: 'GDCD', name: 'GDCD', category: 'Khoa học Xã hội' },
]

export const LANGUAGE_CODE_LABELS = {
  N1: 'N1 - Tiếng Anh',
  N2: 'N2 - Tiếng Nga',
  N3: 'N3 - Tiếng Pháp',
  N4: 'N4 - Tiếng Trung',
  N5: 'N5 - Tiếng Đức',
  N6: 'N6 - Tiếng Nhật',
  N7: 'N7 - Tiếng Hàn',
}

export function formatSubjectDisplayName(subject, languageCode) {
  if (subject.code === 'NGOAI_NGU' && languageCode) {
    const langLabel = LANGUAGE_CODE_LABELS[languageCode] || languageCode
    return `${subject.name} (${langLabel})`
  }
  return subject.name
}

/**
 * Phân loại mức điểm theo đúng 4 ngưỡng quy định tại ui-context.md.
 * Môn không dự thi (null/undefined) luôn trả về dấu '—', tuyệt đối không ép về 0.
 */
export function classifyScoreTier(score) {
  if (score === null || score === undefined || score === '') {
    return {
      key: 'UNTAKEN',
      label: 'Không thi',
      rangeLabel: '—',
      displayScore: '—',
      colorVar: 'var(--text-muted)',
      badgeClass: 'bg-slate-100 text-[var(--text-muted)] border-slate-200',
    }
  }

  const numericScore = Number(score)
  const formatted = numericScore.toFixed(2)

  if (numericScore >= 8.0) {
    return {
      key: 'EXCELLENT',
      label: 'Giỏi',
      rangeLabel: '>= 8.0',
      displayScore: formatted,
      colorVar: 'var(--state-success)',
      badgeClass: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    }
  }

  if (numericScore >= 6.0) {
    return {
      key: 'GOOD',
      label: 'Khá',
      rangeLabel: '6.0 – <8.0',
      displayScore: formatted,
      colorVar: 'var(--state-good)',
      badgeClass: 'bg-blue-50 text-blue-700 border-blue-200',
    }
  }

  if (numericScore >= 4.0) {
    return {
      key: 'AVERAGE',
      label: 'Trung bình',
      rangeLabel: '4.0 – <6.0',
      displayScore: formatted,
      colorVar: 'var(--state-warning)',
      badgeClass: 'bg-amber-50 text-amber-700 border-amber-200',
    }
  }

  return {
    key: 'LOW',
    label: 'Yếu / Kém',
    rangeLabel: '< 4.0',
    displayScore: formatted,
    colorVar: 'var(--state-error)',
    badgeClass: 'bg-red-50 text-red-700 border-red-200',
  }
}

export const MOCK_STUDENTS_BY_SBD = {
  '01000001': {
    sbd: '01000001',
    languageCode: 'N1',
    scores: {
      TOAN: 9.8,
      NGU_VAN: 8.25,
      NGOAI_NGU: 9.4,
      VAT_LI: 10.0,
      HOA_HOC: 9.8,
      SINH_HOC: 8.5,
      LICH_SU: null,
      DIA_LI: null,
      GDCD: null,
    },
  },
  '01000002': {
    sbd: '01000002',
    languageCode: 'N1',
    scores: {
      TOAN: 7.6,
      NGU_VAN: 9.0,
      NGOAI_NGU: 8.8,
      VAT_LI: null,
      HOA_HOC: null,
      SINH_HOC: null,
      LICH_SU: 9.25,
      DIA_LI: 8.75,
      GDCD: 9.5,
    },
  },
  '01000003': {
    sbd: '01000003',
    languageCode: 'N6',
    scores: {
      TOAN: 8.4,
      NGU_VAN: 6.75,
      NGOAI_NGU: 5.2,
      VAT_LI: 7.25,
      HOA_HOC: 5.5,
      SINH_HOC: 3.75,
      LICH_SU: null,
      DIA_LI: null,
      GDCD: null,
    },
  },
  '02001542': {
    sbd: '02001542',
    languageCode: null,
    scores: {
      TOAN: 9.6,
      NGU_VAN: null,
      NGOAI_NGU: null,
      VAT_LI: 9.75,
      HOA_HOC: 9.75,
      SINH_HOC: null,
      LICH_SU: null,
      DIA_LI: null,
      GDCD: null,
    },
  },
}

export const MOCK_OVERVIEW_STATS = {
  totalStudents: 1061605,
  totalScores: 6328410,
  totalSubjects: 9,
  topGroupAScore: 29.6,
}

export const MOCK_SCORE_DISTRIBUTION = [
  {
    subjectCode: 'TOAN',
    subjectName: 'Toán',
    excellent: 198450,
    good: 482310,
    average: 284120,
    low: 81240,
  },
  {
    subjectCode: 'NGU_VAN',
    subjectName: 'Ngữ văn',
    excellent: 276540,
    good: 541200,
    average: 192300,
    low: 39810,
  },
  {
    subjectCode: 'NGOAI_NGU',
    subjectName: 'Ngoại ngữ',
    excellent: 142800,
    good: 268400,
    average: 356900,
    low: 148200,
  },
  {
    subjectCode: 'VAT_LI',
    subjectName: 'Vật lí',
    excellent: 96420,
    good: 168500,
    average: 68400,
    low: 12350,
  },
  {
    subjectCode: 'HOA_HOC',
    subjectName: 'Hóa học',
    excellent: 99120,
    good: 164200,
    average: 69800,
    low: 13410,
  },
  {
    subjectCode: 'SINH_HOC',
    subjectName: 'Sinh học',
    excellent: 34890,
    good: 189400,
    average: 105600,
    low: 12480,
  },
  {
    subjectCode: 'LICH_SU',
    subjectName: 'Lịch sử',
    excellent: 141200,
    good: 368900,
    average: 164500,
    low: 31200,
  },
  {
    subjectCode: 'DIA_LI',
    subjectName: 'Địa lí',
    excellent: 198600,
    good: 412500,
    average: 86400,
    low: 7120,
  },
  {
    subjectCode: 'GDCD',
    subjectName: 'GDCD',
    excellent: 368400,
    good: 201500,
    average: 12800,
    low: 940,
  },
]

export const MOCK_TOP_10_GROUP_A = [
  {
    sbd: '01000001',
    toan: 9.8,
    vatLi: 10.0,
    hoaHoc: 9.8,
    totalScore: 29.6,
    languageCode: 'N1',
  },
  {
    sbd: '26014821',
    toan: 9.6,
    vatLi: 10.0,
    hoaHoc: 10.0,
    totalScore: 29.6,
    languageCode: 'N1',
  },
  {
    sbd: '19008342',
    toan: 9.8,
    vatLi: 10.0,
    hoaHoc: 9.75,
    totalScore: 29.55,
    languageCode: 'N1',
  },
  {
    sbd: '01029415',
    toan: 9.8,
    vatLi: 9.75,
    hoaHoc: 10.0,
    totalScore: 29.55,
    languageCode: 'N1',
  },
  {
    sbd: '28011904',
    toan: 9.6,
    vatLi: 9.75,
    hoaHoc: 10.0,
    totalScore: 29.35,
    languageCode: 'N1',
  },
  {
    sbd: '37006129',
    toan: 9.8,
    vatLi: 9.5,
    hoaHoc: 10.0,
    totalScore: 29.3,
    languageCode: 'N1',
  },
  {
    sbd: '02001542',
    toan: 9.6,
    vatLi: 9.75,
    hoaHoc: 9.75,
    totalScore: 29.1,
    languageCode: null,
  },
  {
    sbd: '15009281',
    toan: 9.6,
    vatLi: 9.5,
    hoaHoc: 10.0,
    totalScore: 29.1,
    languageCode: 'N1',
  },
  {
    sbd: '38014502',
    toan: 9.4,
    vatLi: 9.75,
    hoaHoc: 9.75,
    totalScore: 28.9,
    languageCode: 'N1',
  },
  {
    sbd: '40007813',
    toan: 9.4,
    vatLi: 9.5,
    hoaHoc: 10.0,
    totalScore: 28.9,
    languageCode: 'N6',
  },
]

/**
 * Xếp hạng chuẩn cho danh sách Top Khối A:
 * Các thí sinh có cùng tổng điểm (totalScore) sẽ nhận cùng thứ hạng (Đồng Thủ khoa, Đồng Á khoa, Đồng hạng).
 */
export function computeGroupARanking(candidates = []) {
  const scoreGroupsMap = new Map()

  candidates.forEach((candidate) => {
    const scoreKey = Number(candidate.totalScore).toFixed(2)
    if (!scoreGroupsMap.has(scoreKey)) {
      scoreGroupsMap.set(scoreKey, [])
    }
    scoreGroupsMap.get(scoreKey).push(candidate)
  })

  let cumulativeCount = 0
  const scoreGroups = Array.from(scoreGroupsMap.entries()).map(
    ([scoreKey, groupCandidates]) => {
      // Use candidate's competition rank from API if available, else cumulative rank
      const rank = groupCandidates[0]?.rank ?? (cumulativeCount + 1)
      cumulativeCount += groupCandidates.length
      const isTied = groupCandidates.length > 1

      let roleLabel = `Hạng ${rank}`
      if (rank === 1) {
        roleLabel = isTied
          ? `Đồng Thủ khoa (${groupCandidates.length} thí sinh)`
          : 'Thủ khoa Khối A'
      } else if (rank === 2) {
        roleLabel = isTied
          ? `Đồng Á khoa (${groupCandidates.length} thí sinh)`
          : 'Á khoa Khối A'
      } else if (rank === 3) {
        roleLabel = isTied
          ? `Đồng Hạng 3 (${groupCandidates.length} thí sinh)`
          : 'Hạng 3 Khối A'
      } else if (isTied) {
        roleLabel = `Đồng hạng ${rank}`
      }

      return {
        rank,
        scoreKey,
        isTied,
        roleLabel,
        candidates: groupCandidates,
      }
    },
  )

  const rankedCandidates = candidates.map((candidate, idx) => ({
    ...candidate,
    rank: candidate.rank ?? idx + 1,
  }))

  return {
    rankedCandidates,
    topThreeGroups: scoreGroups.slice(0, 3),
  }
}


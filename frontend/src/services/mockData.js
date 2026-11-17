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
  N1: 'N1 — Tiếng Anh',
  N2: 'N2 — Tiếng Nga',
  N3: 'N3 — Tiếng Pháp',
  N4: 'N4 — Tiếng Trung',
  N5: 'N5 — Tiếng Đức',
  N6: 'N6 — Tiếng Nhật',
  N7: 'N7 — Tiếng Hàn',
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

export const SAMPLE_SBD_LIST = [
  { sbd: '01000001', note: 'Thủ khoa Khối A (Toán, Lí, Hóa xuất sắc)' },
  { sbd: '01000002', note: 'Thí sinh ban Khoa học Xã hội (Sử, Địa, GDCD)' },
  { sbd: '01000003', note: 'Thí sinh có đủ 4 mức điểm (Giỏi, Khá, TB, Yếu)' },
  { sbd: '02001542', note: 'Thí sinh tự do (Chỉ thi 3 môn Khối A)' },
]

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
    rank: 1,
    sbd: '01000001',
    toan: 9.8,
    vatLi: 10.0,
    hoaHoc: 9.8,
    totalScore: 29.6,
    languageCode: 'N1',
  },
  {
    rank: 2,
    sbd: '26014821',
    toan: 9.8,
    vatLi: 9.75,
    hoaHoc: 10.0,
    totalScore: 29.55,
    languageCode: 'N1',
  },
  {
    rank: 3,
    sbd: '19008342',
    toan: 9.8,
    vatLi: 10.0,
    hoaHoc: 9.75,
    totalScore: 29.55,
    languageCode: 'N1',
  },
  {
    rank: 4,
    sbd: '01029415',
    toan: 9.6,
    vatLi: 10.0,
    hoaHoc: 9.75,
    totalScore: 29.35,
    languageCode: 'N1',
  },
  {
    rank: 5,
    sbd: '28011904',
    toan: 9.6,
    vatLi: 9.75,
    hoaHoc: 10.0,
    totalScore: 29.35,
    languageCode: 'N1',
  },
  {
    rank: 6,
    sbd: '37006129',
    toan: 9.8,
    vatLi: 9.5,
    hoaHoc: 10.0,
    totalScore: 29.3,
    languageCode: 'N1',
  },
  {
    rank: 7,
    sbd: '02001542',
    toan: 9.6,
    vatLi: 9.75,
    hoaHoc: 9.75,
    totalScore: 29.1,
    languageCode: null,
  },
  {
    rank: 8,
    sbd: '15009281',
    toan: 9.6,
    vatLi: 9.5,
    hoaHoc: 10.0,
    totalScore: 29.1,
    languageCode: 'N1',
  },
  {
    rank: 9,
    sbd: '38014502',
    toan: 9.4,
    vatLi: 9.75,
    hoaHoc: 9.75,
    totalScore: 28.9,
    languageCode: 'N1',
  },
  {
    rank: 10,
    sbd: '40007813',
    toan: 9.4,
    vatLi: 9.5,
    hoaHoc: 10.0,
    totalScore: 28.9,
    languageCode: 'N6',
  },
]


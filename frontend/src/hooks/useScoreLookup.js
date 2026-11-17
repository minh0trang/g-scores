import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  fetchStudentScoresBySbd,
  isValidSbdFormat,
} from '../services/gscoresService'
import {
  LANGUAGE_CODE_LABELS,
  SUBJECTS,
  classifyScoreTier,
} from '../services/mockData'

const D_BLOCK_BY_LANGUAGE = {
  N1: { code: 'D01', languageName: 'Tiếng Anh' },
  N2: { code: 'D02', languageName: 'Tiếng Nga' },
  N3: { code: 'D03', languageName: 'Tiếng Pháp' },
  N4: { code: 'D04', languageName: 'Tiếng Trung' },
  N5: { code: 'D05', languageName: 'Tiếng Đức' },
  N6: { code: 'D06', languageName: 'Tiếng Nhật' },
  N7: { code: 'D07', languageName: 'Tiếng Hàn' },
}

function buildExamBlocksForStudent(languageCode) {
  const blocks = [
    {
      code: 'A00',
      subjects: ['TOAN', 'VAT_LI', 'HOA_HOC'],
      subjectLabel: 'Toán + Vật lí + Hóa học',
    },
  ]

  // Khối A01 bắt buộc môn Ngoại ngữ phải là Tiếng Anh (N1)
  if (languageCode === 'N1') {
    blocks.push({
      code: 'A01',
      subjects: ['TOAN', 'VAT_LI', 'NGOAI_NGU'],
      subjectLabel: 'Toán + Vật lí + Tiếng Anh',
    })
  }

  blocks.push(
    {
      code: 'B00',
      subjects: ['TOAN', 'HOA_HOC', 'SINH_HOC'],
      subjectLabel: 'Toán + Hóa học + Sinh học',
    },
    {
      code: 'C00',
      subjects: ['NGU_VAN', 'LICH_SU', 'DIA_LI'],
      subjectLabel: 'Ngữ văn + Lịch sử + Địa lí',
    },
  )

  // Khối D01..D07 tương ứng đúng mã Ngoại ngữ N1..N7 của thí sinh
  const dBlockMeta = D_BLOCK_BY_LANGUAGE[languageCode]
  if (dBlockMeta) {
    blocks.push({
      code: dBlockMeta.code,
      subjects: ['TOAN', 'NGU_VAN', 'NGOAI_NGU'],
      subjectLabel: `Toán + Ngữ văn + ${dBlockMeta.languageName}`,
    })
  }

  return blocks
}

export function useScoreLookup(initialSbd = '') {
  const normalizedInitial = String(initialSbd ?? '').trim()
  const hasInitialValidSbd = isValidSbdFormat(normalizedInitial)

  const [sbdInput, setSbdInput] = useState(normalizedInitial)
  const [studentData, setStudentData] = useState(null)
  const [loading, setLoading] = useState(hasInitialValidSbd)
  const [error, setError] = useState(null)
  const [hasSearched, setHasSearched] = useState(hasInitialValidSbd)

  useEffect(() => {
    if (!hasInitialValidSbd) return

    let isCancelled = false

    async function loadInitialStudent() {
      try {
        const result = await fetchStudentScoresBySbd(normalizedInitial)
        if (!isCancelled) {
          setStudentData(result)
          setError(null)
        }
      } catch (err) {
        if (!isCancelled) {
          setStudentData(null)
          setError(
            err?.message ||
              'Không thể tra cứu điểm thi lúc này. Vui lòng thử lại.',
          )
        }
      } finally {
        if (!isCancelled) {
          setLoading(false)
        }
      }
    }

    loadInitialStudent()

    return () => {
      isCancelled = true
    }
  }, [hasInitialValidSbd, normalizedInitial])

  const lookupBySbd = useCallback(
    async (overrideSbd) => {
      const targetSbd = String(overrideSbd ?? sbdInput).trim()
      if (overrideSbd !== undefined) {
        setSbdInput(targetSbd)
      }

      setHasSearched(true)

      if (!targetSbd) {
        setError('Vui lòng nhập Số báo danh (8 chữ số) để tra cứu.')
        setStudentData(null)
        return
      }

      if (!isValidSbdFormat(targetSbd)) {
        setError(
          'Số báo danh không hợp lệ. Vui lòng nhập đúng 8 chữ số (ví dụ: 01000001).',
        )
        setStudentData(null)
        return
      }

      setLoading(true)
      setError(null)

      try {
        const result = await fetchStudentScoresBySbd(targetSbd)
        setStudentData(result)
      } catch (err) {
        setStudentData(null)
        setError(
          err?.message || 'Không thể tra cứu điểm thi lúc này. Vui lòng thử lại.',
        )
      } finally {
        setLoading(false)
      }
    },
    [sbdInput],
  )

  const resetLookup = useCallback(() => {
    setSbdInput('')
    setStudentData(null)
    setError(null)
    setHasSearched(false)
  }, [])

  // Chỉ lấy các môn thí sinh có điểm dự thi (bỏ qua các môn không thi)
  const subjectScores = useMemo(() => {
    if (!studentData) return []

    const rawScores = studentData.scores || {}

    return SUBJECTS.filter(
      (subject) =>
        rawScores[subject.code] !== null &&
        rawScores[subject.code] !== undefined &&
        rawScores[subject.code] !== '',
    ).map((subject) => {
      const rawValue = rawScores[subject.code]
      const tier = classifyScoreTier(rawValue)

      return {
        ...subject,
        score: rawValue,
        tier,
      }
    })
  }, [studentData])

  // Tính tổng điểm theo các khối xét tuyển đúng với mã ngoại ngữ của thí sinh
  const blockScores = useMemo(() => {
    if (!studentData) return []

    const rawScores = studentData.scores || {}
    const candidateBlocks = buildExamBlocksForStudent(studentData.languageCode)

    return candidateBlocks
      .filter((block) =>
        block.subjects.every(
          (code) =>
            rawScores[code] !== null &&
            rawScores[code] !== undefined &&
            rawScores[code] !== '',
        ),
      )
      .map((block) => {
        const total = block.subjects.reduce(
          (sum, code) => sum + Number(rawScores[code]),
          0,
        )
        const tier = classifyScoreTier(total / 3)

        return {
          ...block,
          totalScore: total.toFixed(2),
          tier,
        }
      })
  }, [studentData])

  const summary = useMemo(() => {
    if (!studentData) return null

    const totalTaken = subjectScores.length
    const averageScore =
      totalTaken > 0
        ? (
            subjectScores.reduce((acc, cur) => acc + Number(cur.score), 0) /
            totalTaken
          ).toFixed(2)
        : '—'

    const languageLabel = studentData.languageCode
      ? LANGUAGE_CODE_LABELS[studentData.languageCode] ||
        studentData.languageCode
      : null

    return {
      sbd: studentData.sbd,
      languageCode: studentData.languageCode || null,
      languageLabel,
      totalTaken,
      averageScore,
    }
  }, [studentData, subjectScores])

  return {
    sbdInput,
    setSbdInput,
    studentData,
    subjectScores,
    blockScores,
    summary,
    loading,
    error,
    hasSearched,
    lookupBySbd,
    resetLookup,
  }
}

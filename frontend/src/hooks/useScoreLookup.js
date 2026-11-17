import { useCallback, useMemo, useState } from 'react'
import {
  fetchStudentScoresBySbd,
  isValidSbdFormat,
} from '../services/gscoresService'
import {
  LANGUAGE_CODE_LABELS,
  SUBJECTS,
  classifyScoreTier,
} from '../services/mockData'

export function useScoreLookup(initialSbd = '') {
  const [sbdInput, setSbdInput] = useState(initialSbd)
  const [studentData, setStudentData] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [hasSearched, setHasSearched] = useState(false)

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

  const subjectScores = useMemo(() => {
    if (!studentData) return []

    const rawScores = studentData.scores || {}

    return SUBJECTS.map((subject) => {
      const rawValue = rawScores[subject.code] ?? null
      const tier = classifyScoreTier(rawValue)

      return {
        ...subject,
        score: rawValue,
        tier,
      }
    })
  }, [studentData])

  const summary = useMemo(() => {
    if (!studentData) return null

    const takenSubjects = subjectScores.filter((item) => item.score !== null)
    const totalTaken = takenSubjects.length
    const averageScore =
      totalTaken > 0
        ? (
            takenSubjects.reduce((acc, cur) => acc + Number(cur.score), 0) /
            totalTaken
          ).toFixed(2)
        : '—'

    const languageLabel = studentData.languageCode
      ? LANGUAGE_CODE_LABELS[studentData.languageCode] ||
        studentData.languageCode
      : '— (Miễn thi / Không đăng ký)'

    return {
      sbd: studentData.sbd,
      languageCode: studentData.languageCode || '—',
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
    summary,
    loading,
    error,
    hasSearched,
    lookupBySbd,
    resetLookup,
  }
}


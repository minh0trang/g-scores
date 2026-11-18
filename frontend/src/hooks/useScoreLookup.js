import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  fetchStudentScoresBySbd,
  isValidSbdFormat,
} from '../services/gscoresService'
import {
  LANGUAGE_CODE_LABELS,
  SUBJECTS,
  classifyScoreTier,
  formatSubjectDisplayName,
} from '../services/mockData'

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

  // Hiển thị các môn có điểm dự thi + môn Ngoại ngữ
  const subjectScores = useMemo(() => {
    if (!studentData) return []

    const rawScores = studentData.scores || {}
    const languageCode = studentData.languageCode

    return SUBJECTS.filter((subject) => {
      const hasScore =
        rawScores[subject.code] !== null &&
        rawScores[subject.code] !== undefined &&
        rawScores[subject.code] !== ''
      return hasScore || subject.code === 'NGOAI_NGU'
    }).map((subject) => {
      const rawValue = rawScores[subject.code] ?? null
      const tier = classifyScoreTier(rawValue)

      return {
        ...subject,
        name: formatSubjectDisplayName(subject, languageCode),
        score: rawValue,
        tier,
      }
    })
  }, [studentData])

  // Single Source of Truth: Dùng trực tiếp mảng blocks[] trả về từ Backend API
  const blockScores = useMemo(() => {
    if (!studentData || !Array.isArray(studentData.blocks)) return []

    return studentData.blocks.map((b) => {
      const numTotal = Number(b.totalScore)
      const tier = classifyScoreTier(numTotal / 3)

      return {
        code: b.blockCode,
        name: b.blockName,
        subjectLabel: b.blockName,
        totalScore: numTotal.toFixed(2),
        nationalRank: b.nationalRank,
        nationalTotal: b.nationalTotal,
        provincialRank: b.provincialRank,
        provincialTotal: b.provincialTotal,
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

    const languageLabel =
      studentData.languageDisplayName ||
      (studentData.languageCode
        ? LANGUAGE_CODE_LABELS[studentData.languageCode] ||
          studentData.languageCode
        : null)

    return {
      sbd: studentData.sbd,
      provinceCode: studentData.provinceCode,
      provinceName: studentData.provinceName,
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

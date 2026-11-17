import { useMemo, useState } from 'react'
import { AlertCircle, Loader2 } from 'lucide-react'
import ScoreDistributionChart from '../components/ScoreDistributionChart'
import SubjectSelectorBar from '../components/SubjectSelectorBar'
import SubjectTierSummary from '../components/SubjectTierSummary'
import { useReportData } from '../hooks/useReportData'

export default function Dashboard() {
  const { scoreDistribution, loading, error } = useReportData()
  const [selectedSubjectCode, setSelectedSubjectCode] = useState('ALL')

  const selectedSubjectData = useMemo(() => {
    if (selectedSubjectCode === 'ALL') return null
    return (
      scoreDistribution.find(
        (item) => item.subjectCode === selectedSubjectCode,
      ) || null
    )
  }, [scoreDistribution, selectedSubjectCode])

  const aggregatedStats = useMemo(() => {
    if (selectedSubjectData) {
      return selectedSubjectData
    }

    return scoreDistribution.reduce(
      (acc, cur) => ({
        excellent: acc.excellent + (cur.excellent || 0),
        good: acc.good + (cur.good || 0),
        average: acc.average + (cur.average || 0),
        low: acc.low + (cur.low || 0),
      }),
      { excellent: 0, good: 0, average: 0, low: 0 },
    )
  }, [scoreDistribution, selectedSubjectData])

  if (loading) {
    return (
      <div className="py-16 flex flex-col items-center justify-center text-center">
        <Loader2
          className="h-8 w-8 text-[var(--accent-primary)] animate-spin mb-2.5"
          strokeWidth={2}
        />
        <p className="text-sm font-medium text-[var(--text-primary)]">
          Đang tải dữ liệu thống kê môn học...
        </p>
      </div>
    )
  }

  if (error) {
    return (
      <div
        role="alert"
        className="flex items-center gap-2.5 p-4 rounded-xl bg-red-50 border border-red-200 text-red-800 text-sm"
      >
        <AlertCircle
          className="h-5 w-5 text-[var(--state-error)] shrink-0"
          strokeWidth={2}
        />
        <span>{error}</span>
      </div>
    )
  }

  return (
    <div className="flex-1 flex flex-col gap-3.5 min-h-0">
      <SubjectSelectorBar
        subjects={scoreDistribution}
        selectedCode={selectedSubjectCode}
        onSelect={setSelectedSubjectCode}
      />

      <SubjectTierSummary stats={aggregatedStats} />

      <ScoreDistributionChart
        distribution={scoreDistribution}
        selectedSubjectCode={selectedSubjectCode}
        selectedSubjectData={selectedSubjectData}
      />
    </div>
  )
}

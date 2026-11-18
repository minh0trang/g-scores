import { AlertCircle, Loader2 } from 'lucide-react'
import GroupALeaderboardTable from '../components/GroupALeaderboardTable'
import TopThreeGroupACards from '../components/TopThreeGroupACards'
import { useReportData } from '../hooks/useReportData'

export default function Leaderboard() {
  const {
    top10GroupA,
    provinces,
    selectedProvince,
    setSelectedProvince,
    loading,
    error,
  } = useReportData()

  if (loading) {
    return (
      <div className="py-16 flex flex-col items-center justify-center text-center">
        <Loader2
          className="h-8 w-8 text-[var(--accent-primary)] animate-spin mb-2.5"
          strokeWidth={2}
        />
        <p className="text-sm font-medium text-[var(--text-primary)]">
          Đang tải bảng xếp hạng Top 10 Khối A...
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
      <TopThreeGroupACards topCandidates={top10GroupA} />
      <GroupALeaderboardTable
        topCandidates={top10GroupA}
        provinces={provinces}
        selectedProvince={selectedProvince}
        onSelectProvince={setSelectedProvince}
      />
    </div>
  )
}

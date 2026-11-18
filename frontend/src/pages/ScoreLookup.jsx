import { useSearchParams } from 'react-router-dom'
import { FileText, Loader2, Search } from 'lucide-react'
import BlockScoreTable from '../components/BlockScoreTable'
import SbdSearchForm from '../components/SbdSearchForm'
import SubjectScoreTable from '../components/SubjectScoreTable'
import { useScoreLookup } from '../hooks/useScoreLookup'

export default function ScoreLookup() {
  const [searchParams, setSearchParams] = useSearchParams()
  const initialQuerySbd = searchParams.get('sbd') || ''

  const {
    sbdInput,
    setSbdInput,
    subjectScores,
    blockScores,
    summary,
    loading,
    error,
    hasSearched,
    lookupBySbd,
    resetLookup,
  } = useScoreLookup(initialQuerySbd)

  const handleSubmit = (e) => {
    e.preventDefault()
    const trimmed = sbdInput.trim()
    if (trimmed) {
      setSearchParams({ sbd: trimmed }, { replace: true })
    }
    lookupBySbd(trimmed)
  }

  const handleClear = () => {
    setSearchParams({}, { replace: true })
    resetLookup()
  }

  return (
    <div className="flex-1 flex flex-col gap-2.5 sm:gap-3.5 min-h-0">
      <SbdSearchForm
        sbdInput={sbdInput}
        onSbdChange={setSbdInput}
        onSubmit={handleSubmit}
        onClear={handleClear}
        loading={loading}
        error={error}
        hasSearched={hasSearched}
        summary={summary}
      />

      <section className="flex-1 flex flex-col min-h-0 bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-3 sm:p-4 md:p-5 shadow-2xs">
        <div className="flex items-center gap-1.5 sm:gap-2 mb-2.5 sm:mb-3">
          <FileText
            className="h-4.5 w-4.5 sm:h-5 sm:w-5 text-[var(--accent-primary)] shrink-0"
            strokeWidth={2}
          />
          <h2 className="text-sm sm:text-base font-semibold text-[var(--text-primary)]">
            Bảng điểm chi tiết
          </h2>
        </div>

        {loading && (
          <div className="flex-1 flex flex-col items-center justify-center text-center">
            <Loader2
              className="h-7 w-7 text-[var(--accent-primary)] animate-spin mb-2"
              strokeWidth={2}
            />
            <p className="text-sm font-medium text-[var(--text-primary)]">
              Đang tải dữ liệu điểm thi...
            </p>
          </div>
        )}

        {!loading && !summary && (
          <div className="flex-1 flex flex-col items-center justify-center px-4 border border-dashed border-[var(--border-default)] rounded-xl bg-slate-50/50 text-center">
            <Search
              className="h-7 w-7 text-[var(--text-muted)] mx-auto mb-2 opacity-75"
              strokeWidth={2}
            />
            <p className="text-sm font-medium text-[var(--text-primary)]">
              Nhập Số báo danh để xem kết quả điểm thi
            </p>
          </div>
        )}

        {!loading && summary && (
          <div className="flex-1 min-h-0 grid grid-cols-1 lg:grid-cols-2 gap-4 items-stretch">
            <SubjectScoreTable
              subjectScores={subjectScores}
              summary={summary}
            />
            <BlockScoreTable
              blockScores={blockScores}
              provinceName={summary?.provinceName}
            />
          </div>
        )}
      </section>
    </div>
  )
}

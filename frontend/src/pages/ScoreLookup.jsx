import { useSearchParams } from 'react-router-dom'
import { FileText, Globe, Hash, Loader2, Search } from 'lucide-react'
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

  const handleSelectSample = (sampleSbd) => {
    setSearchParams({ sbd: sampleSbd }, { replace: true })
    lookupBySbd(sampleSbd)
  }

  const handleClear = () => {
    setSearchParams({}, { replace: true })
    resetLookup()
  }

  return (
    <div className="space-y-4">
      <SbdSearchForm
        sbdInput={sbdInput}
        onSbdChange={setSbdInput}
        onSubmit={handleSubmit}
        onSelectSample={handleSelectSample}
        onClear={handleClear}
        loading={loading}
        error={error}
        hasSearched={hasSearched}
        activeSbd={summary?.sbd}
      />

      <section className="bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-4 md:p-5 shadow-2xs">
        <div className="flex flex-wrap items-center justify-between gap-2 mb-3">
          <div className="flex items-center gap-2">
            <FileText
              className="h-5 w-5 text-[var(--accent-primary)]"
              strokeWidth={2}
            />
            <h2 className="text-base font-semibold text-[var(--text-primary)]">
              Bảng điểm chi tiết
            </h2>
          </div>

          {summary && (
            <div className="flex flex-wrap items-center gap-2 text-xs">
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-slate-100 font-medium text-[var(--text-primary)]">
                <Hash
                  className="h-3.5 w-3.5 text-[var(--accent-primary)]"
                  strokeWidth={2}
                />
                <span>SBD:</span>
                <span className="font-mono font-bold text-[var(--accent-primary)]">
                  {summary.sbd}
                </span>
              </span>

              {summary.languageLabel && (
                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-slate-100 font-medium text-[var(--text-primary)]">
                  <Globe
                    className="h-3.5 w-3.5 text-[var(--accent-primary)]"
                    strokeWidth={2}
                  />
                  <span>{summary.languageLabel}</span>
                </span>
              )}
            </div>
          )}
        </div>

        {loading && (
          <div className="py-8 flex flex-col items-center justify-center text-center">
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
          <div className="py-8 px-4 border border-dashed border-[var(--border-default)] rounded-xl bg-slate-50/50 text-center">
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
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 items-start">
            <SubjectScoreTable
              subjectScores={subjectScores}
              summary={summary}
            />
            <BlockScoreTable blockScores={blockScores} />
          </div>
        )}
      </section>
    </div>
  )
}

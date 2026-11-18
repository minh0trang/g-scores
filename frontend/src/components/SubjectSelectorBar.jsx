import { BookOpen } from 'lucide-react'
import ProvinceSelector from './ProvinceSelector'

export default function SubjectSelectorBar({
  subjects,
  selectedCode,
  onSelect,
  provinces = [],
  selectedProvince = '',
  onSelectProvince,
}) {
  return (
    <section className="bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-2.5 sm:p-3 md:p-3.5 shadow-2xs flex flex-wrap items-center justify-between gap-2.5 sm:gap-3">
      <div className="flex items-center justify-between sm:justify-start w-full sm:w-auto gap-2 sm:gap-2.5">
        <div className="flex items-center gap-1.5 sm:gap-2">
          <BookOpen
            className="h-4.5 w-4.5 sm:h-5 sm:w-5 text-[var(--accent-primary)] shrink-0"
            strokeWidth={2}
          />
          <span className="text-xs sm:text-sm font-semibold text-[var(--text-primary)] hidden md:inline">
            Thống kê môn học:
          </span>
        </div>

        {provinces && provinces.length > 0 && (
          <ProvinceSelector
            provinces={provinces}
            selectedProvince={selectedProvince}
            onSelectProvince={onSelectProvince}
          />
        )}
      </div>

      <div className="flex items-center gap-1 sm:gap-1.5 overflow-x-auto w-full sm:w-auto pb-1 sm:pb-0 scrollbar-none flex-nowrap sm:flex-wrap">
        <button
          type="button"
          onClick={() => onSelect('ALL')}
          className={`px-2.5 py-1 sm:px-3 sm:py-1.5 rounded-lg text-[11px] sm:text-xs font-medium border transition-all cursor-pointer whitespace-nowrap shrink-0 ${
            selectedCode === 'ALL'
              ? 'bg-[var(--bg-brand)] text-white border-[var(--bg-brand)] shadow-2xs'
              : 'bg-slate-50 text-[var(--text-primary)] border-[var(--border-default)] md:hover:bg-slate-100'
          }`}
        >
          Tất cả
        </button>

        {subjects.map((subj) => {
          const isActive = selectedCode === subj.subjectCode
          return (
            <button
              key={subj.subjectCode}
              type="button"
              onClick={() => onSelect(subj.subjectCode)}
              className={`px-2.5 py-1 sm:px-3 sm:py-1.5 rounded-lg text-[11px] sm:text-xs font-medium border transition-all cursor-pointer whitespace-nowrap shrink-0 ${
                isActive
                  ? 'bg-[var(--accent-primary)] text-white border-[var(--accent-primary)] shadow-2xs'
                  : 'bg-slate-50 text-[var(--text-primary)] border-[var(--border-default)] md:hover:bg-slate-100'
              }`}
            >
              {subj.subjectName}
            </button>
          )
        })}
      </div>
    </section>
  )
}

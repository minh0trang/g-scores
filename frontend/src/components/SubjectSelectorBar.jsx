import { BookOpen } from 'lucide-react'

export default function SubjectSelectorBar({
  subjects,
  selectedCode,
  onSelect,
}) {
  return (
    <section className="bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-3.5 md:p-4 shadow-2xs flex flex-wrap items-center justify-between gap-3">
      <div className="flex items-center gap-2">
        <BookOpen
          className="h-5 w-5 text-[var(--accent-primary)]"
          strokeWidth={2}
        />
        <span className="text-sm font-semibold text-[var(--text-primary)]">
          Thống kê theo môn học:
        </span>
      </div>

      <div className="flex flex-wrap items-center gap-1.5">
        <button
          type="button"
          onClick={() => onSelect('ALL')}
          className={`px-3 py-1.5 rounded-lg text-xs font-medium border transition-all cursor-pointer ${selectedCode === 'ALL'
              ? 'bg-[var(--bg-brand)] text-white border-[var(--bg-brand)] shadow-2xs'
              : 'bg-slate-50 text-[var(--text-primary)] border-[var(--border-default)] hover:bg-slate-100'
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
              className={`px-3 py-1.5 rounded-lg text-xs font-medium border transition-all cursor-pointer ${isActive
                  ? 'bg-[var(--accent-primary)] text-white border-[var(--accent-primary)] shadow-2xs'
                  : 'bg-slate-50 text-[var(--text-primary)] border-[var(--border-default)] hover:bg-slate-100'
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


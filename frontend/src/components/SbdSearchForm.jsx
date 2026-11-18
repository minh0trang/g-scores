import {
  AlertCircle,
  Loader2,
  MapPin,
  RotateCcw,
  Search,
  X,
} from 'lucide-react'

export default function SbdSearchForm({
  sbdInput,
  onSbdChange,
  onSubmit,
  onClear,
  loading,
  error,
  hasSearched,
  summary,
}) {
  return (
    <section className="bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-3 sm:p-4 md:p-5 shadow-2xs">
      <h2 className="text-sm sm:text-base font-semibold text-[var(--text-primary)] mb-2.5 sm:mb-3">
        Thông tin đăng ký dự thi
      </h2>

      <form onSubmit={onSubmit} noValidate>
        <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-2.5">
          <label
            htmlFor="sbd-input"
            className="text-xs sm:text-sm font-medium text-[var(--text-primary)] shrink-0"
          >
            Số báo danh:
          </label>

          <div className="relative flex-1 max-w-full sm:max-w-sm">
            <input
              id="sbd-input"
              type="text"
              inputMode="numeric"
              maxLength={8}
              value={sbdInput}
              onChange={(e) => onSbdChange(e.target.value)}
              placeholder="Nhập Số báo danh (VD: 01000001)"
              className="w-full pl-3 sm:pl-3.5 pr-8 sm:pr-9 py-1.5 sm:py-2 rounded-lg border border-[var(--border-default)] bg-[var(--bg-surface)] text-base md:text-sm font-medium text-[var(--text-primary)] placeholder:font-normal placeholder:text-[var(--text-muted)] focus:outline-none focus:ring-2 focus:ring-[var(--accent-primary)] focus:border-transparent transition-shadow"
            />
            {sbdInput && (
              <button
                type="button"
                onClick={onClear}
                aria-label="Xóa Số báo danh"
                className="absolute inset-y-0 right-0 pr-2 sm:pr-2.5 flex items-center text-[var(--text-muted)] md:hover:text-[var(--text-primary)] cursor-pointer"
              >
                <X className="h-4 w-4" strokeWidth={2} />
              </button>
            )}
          </div>

          <div className="flex items-center gap-2 w-full sm:w-auto">
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center justify-center gap-1.5 sm:gap-2 px-3.5 sm:px-4 py-1.5 sm:py-2 rounded-lg bg-[var(--accent-primary)] text-white text-xs sm:text-sm font-medium md:hover:bg-blue-700 disabled:opacity-60 transition-colors shadow-xs cursor-pointer flex-1 sm:flex-initial"
            >
              {loading ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" strokeWidth={2} />
                  <span>Đang tìm...</span>
                </>
              ) : (
                <>
                  <Search className="h-4 w-4" strokeWidth={2} />
                  <span>Tra cứu</span>
                </>
              )}
            </button>

            {hasSearched && (
              <button
                type="button"
                onClick={onClear}
                className="inline-flex items-center justify-center gap-1.5 px-2.5 sm:px-3 py-1.5 sm:py-2 rounded-lg border border-[var(--border-default)] bg-[var(--bg-surface)] text-xs sm:text-sm font-medium text-[var(--text-muted)] md:hover:text-[var(--text-primary)] md:hover:bg-slate-50 transition-colors cursor-pointer"
              >
                <RotateCcw className="h-4 w-4" strokeWidth={2} />
                <span>Làm mới</span>
              </button>
            )}
          </div>
        </div>
      </form>

      {summary && (
        <div className="mt-3 pt-2.5 sm:mt-3.5 sm:pt-3 border-t border-[var(--border-default)] flex flex-wrap items-center gap-2 sm:gap-2.5 text-[11px] sm:text-xs md:text-sm">
          <div className="inline-flex items-center gap-1 px-2 py-0.5 sm:px-2.5 sm:py-1 rounded-md bg-blue-50 text-[var(--accent-primary)] font-semibold border border-blue-200">
            <span>SBD:</span>
            <span>{summary.sbd}</span>
          </div>

          {summary.provinceName && (
            <div className="inline-flex items-center gap-1 px-2 py-0.5 sm:px-2.5 sm:py-1 rounded-md bg-slate-100 text-[var(--text-primary)] font-medium border border-slate-200">
              <MapPin className="h-3.5 w-3.5 text-[var(--accent-primary)] shrink-0" strokeWidth={2} />
              <span className="text-[var(--text-muted)]">Tỉnh/Thành phố:</span>
              <span className="font-semibold">{summary.provinceCode} - {summary.provinceName}</span>
            </div>
          )}

          {summary.languageLabel && (
            <div className="inline-flex items-center gap-1 px-2 py-0.5 sm:px-2.5 sm:py-1 rounded-md bg-emerald-50 text-emerald-800 font-medium border border-emerald-200">
              <span className="text-emerald-700">Ngoại ngữ:</span>
              <span>{summary.languageLabel}</span>
            </div>
          )}
        </div>
      )}

      {error && (
        <div
          role="alert"
          className="mt-3 flex items-center gap-2.5 px-3.5 py-2.5 rounded-lg bg-red-50 border border-red-200 text-red-800 text-xs sm:text-sm"
        >
          <AlertCircle
            className="h-4 w-4 text-[var(--state-error)] shrink-0"
            strokeWidth={2}
          />
          <span>{error}</span>
        </div>
      )}
    </section>
  )
}

import { MapPin } from 'lucide-react'

export default function ProvinceSelector({
  provinces = [],
  selectedProvince = '',
  onSelectProvince,
  disabled = false,
  className = '',
}) {
  return (
    <div className={`relative inline-flex items-center ${className}`}>
      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[var(--accent-primary)]">
        <MapPin className="h-4 w-4" strokeWidth={2} />
      </div>
      <select
        value={selectedProvince}
        onChange={(e) => onSelectProvince(e.target.value)}
        disabled={disabled}
        aria-label="Lọc theo tỉnh thành"
        className="w-full sm:w-auto appearance-none pl-9 pr-8 py-2 rounded-lg border border-[var(--border-default)] bg-[var(--bg-surface)] text-sm font-medium text-[var(--text-primary)] hover:border-slate-300 focus:outline-none focus:ring-2 focus:ring-[var(--accent-primary)] focus:border-transparent transition-all cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed shadow-2xs"
      >
        <option value="">Toàn quốc (63 tỉnh/thành)</option>
        {provinces.map((p) => (
          <option key={p.code} value={p.code}>
            {p.code} - {p.name}
          </option>
        ))}
      </select>
      <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none text-[var(--text-muted)] text-xs">
        ▼
      </div>
    </div>
  )
}

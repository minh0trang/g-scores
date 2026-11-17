const TIER_META = [
  {
    key: 'excellent',
    label: 'Mức Giỏi (>= 8.0)',
    colorVar: 'var(--state-success)',
    bgClass: 'bg-emerald-50/70 border-emerald-200',
  },
  {
    key: 'good',
    label: 'Mức Khá (6.0 – <8.0)',
    colorVar: 'var(--state-good)',
    bgClass: 'bg-blue-50/70 border-blue-200',
  },
  {
    key: 'average',
    label: 'Trung bình (4.0 – <6.0)',
    colorVar: 'var(--state-warning)',
    bgClass: 'bg-amber-50/70 border-amber-200',
  },
  {
    key: 'low',
    label: 'Yếu / Kém (< 4.0)',
    colorVar: 'var(--state-error)',
    bgClass: 'bg-red-50/70 border-red-200',
  },
]

export default function SubjectTierSummary({ stats }) {
  const total =
    (stats?.excellent || 0) +
    (stats?.good || 0) +
    (stats?.average || 0) +
    (stats?.low || 0)

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
      {TIER_META.map((tier) => {
        const count = stats?.[tier.key] || 0
        const percent = total > 0 ? ((count / total) * 100).toFixed(1) : '0.0'

        return (
          <div
            key={tier.key}
            className={`rounded-xl border p-3.5 bg-[var(--bg-surface)] border-[var(--border-default)] shadow-2xs`}
          >
            <div className="flex items-center justify-between gap-2">
              <span className="text-xs font-medium text-[var(--text-muted)]">
                {tier.label}
              </span>
              <span
                className="text-xs font-semibold px-2 py-0.5 rounded-md border"
                style={{
                  color: tier.colorVar,
                  borderColor: 'var(--border-default)',
                }}
              >
                {percent}%
              </span>
            </div>

            <p
              className="text-xl font-bold mt-1.5"
              style={{ color: tier.colorVar }}
            >
              {count.toLocaleString('vi-VN')}
            </p>
            <p className="text-xs text-[var(--text-muted)] mt-0.5">
              lượt bài thi
            </p>
          </div>
        )
      })}
    </div>
  )
}


import { Layers } from 'lucide-react'

export default function BlockScoreTable({ blockScores }) {
  return (
    <div className="overflow-x-auto rounded-lg border border-[var(--border-default)]">
      <table className="w-full text-left border-collapse text-sm">
        <thead>
          <tr className="bg-slate-50 border-b border-[var(--border-default)] text-xs font-semibold uppercase tracking-wider text-[var(--text-muted)]">
            <th className="py-2.5 px-4 w-28">Khối</th>
            <th className="py-2.5 px-4">Tổ hợp môn</th>
            <th className="py-2.5 px-4 text-right">Tổng điểm</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-[var(--border-default)]">
          {blockScores.map((block) => (
            <tr
              key={block.code}
              className="hover:bg-slate-50/80 transition-colors"
            >
              <td className="py-2 px-4 font-semibold text-[var(--text-primary)]">
                <span className="inline-flex items-center gap-1.5">
                  <Layers
                    className="h-3.5 w-3.5 text-[var(--accent-primary)]"
                    strokeWidth={2}
                  />
                  <span>{block.code}</span>
                </span>
              </td>
              <td className="py-2 px-4 text-xs text-[var(--text-muted)]">
                {block.subjectLabel}
              </td>
              <td className="py-2 px-4 text-right">
                <span
                  className="text-base font-bold"
                  style={{ color: block.tier.colorVar }}
                >
                  {block.totalScore}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}


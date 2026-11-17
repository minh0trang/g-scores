import { Layers } from 'lucide-react'

export default function BlockScoreTable({ blockScores }) {
  const bestBlock =
    blockScores && blockScores.length > 0
      ? blockScores.reduce((best, cur) =>
          Number(cur.totalScore) > Number(best.totalScore) ? cur : best,
        )
      : null

  return (
    <div className="h-full flex flex-col justify-between overflow-hidden rounded-lg border border-[var(--border-default)] bg-[var(--bg-surface)]">
      <div className="overflow-x-auto">
        <table className="w-full text-center border-collapse text-sm">
          <thead>
            <tr className="bg-slate-50 border-b border-[var(--border-default)] text-xs font-semibold uppercase tracking-wider text-[var(--text-muted)]">
              <th className="py-2.5 px-4 w-28 text-center">Khối</th>
              <th className="py-2.5 px-4 text-center">Tổ hợp môn</th>
              <th className="py-2.5 px-4 text-center">Tổng điểm</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--border-default)]">
            {blockScores.map((block) => (
              <tr
                key={block.code}
                className="hover:bg-slate-50/80 transition-colors border-b border-[var(--border-default)] last:border-b-0"
              >
                <td className="py-2.5 px-4 text-center font-semibold text-[var(--text-primary)]">
                  <span className="inline-flex items-center justify-center gap-1.5">
                    <Layers
                      className="h-3.5 w-3.5 text-[var(--accent-primary)]"
                      strokeWidth={2}
                    />
                    <span>{block.code}</span>
                  </span>
                </td>
                <td className="py-2.5 px-4 text-center text-xs text-[var(--text-muted)]">
                  {block.subjectLabel}
                </td>
                <td className="py-2.5 px-4 text-center">
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

      {bestBlock && (
        <div className="mt-auto bg-slate-50 border-t border-[var(--border-default)] py-2.5 px-5 flex items-center justify-between">
          <span className="text-xs font-semibold uppercase tracking-wider text-[var(--text-muted)]">
            Khối điểm cao nhất ({bestBlock.code})
          </span>
          <span
            className="text-base font-bold"
            style={{ color: bestBlock.tier.colorVar }}
          >
            {bestBlock.totalScore}
          </span>
        </div>
      )}
    </div>
  )
}

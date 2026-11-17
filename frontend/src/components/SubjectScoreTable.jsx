import { classifyScoreTier } from '../services/mockData'

export default function SubjectScoreTable({ subjectScores, summary }) {
  const averageTier = summary ? classifyScoreTier(summary.averageScore) : null

  return (
    <div className="overflow-x-auto rounded-lg border border-[var(--border-default)]">
      <table className="w-full text-left border-collapse text-sm">
        <thead>
          <tr className="bg-slate-50 border-b border-[var(--border-default)] text-xs font-semibold uppercase tracking-wider text-[var(--text-muted)]">
            <th className="py-2.5 px-4 w-14 text-center">STT</th>
            <th className="py-2.5 px-4">Môn thi</th>
            <th className="py-2.5 px-4 text-right">Điểm số</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-[var(--border-default)]">
          {subjectScores.map((item, index) => (
            <tr
              key={item.code}
              className="hover:bg-slate-50/80 transition-colors"
            >
              <td className="py-2 px-4 text-center text-xs text-[var(--text-muted)]">
                {index + 1}
              </td>
              <td className="py-2 px-4 font-medium text-[var(--text-primary)]">
                {item.name}
              </td>
              <td className="py-2 px-4 text-right">
                <span
                  className="text-base font-bold"
                  style={{ color: item.tier.colorVar }}
                >
                  {item.tier.displayScore}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
        {averageTier && summary && (
          <tfoot>
            <tr className="bg-slate-50/90 border-t border-[var(--border-default)] font-semibold">
              <td
                colSpan={2}
                className="py-2 px-4 text-xs uppercase tracking-wider text-[var(--text-muted)]"
              >
                Điểm trung bình ({summary.totalTaken} môn)
              </td>
              <td className="py-2 px-4 text-right">
                <span
                  className="text-base font-bold"
                  style={{ color: averageTier.colorVar }}
                >
                  {summary.averageScore}
                </span>
              </td>
            </tr>
          </tfoot>
        )}
      </table>
    </div>
  )
}


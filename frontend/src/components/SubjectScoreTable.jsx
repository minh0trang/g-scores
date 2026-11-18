import { classifyScoreTier } from '../services/mockData'

export default function SubjectScoreTable({ subjectScores, summary }) {
  const averageTier = summary ? classifyScoreTier(summary.averageScore) : null

  return (
    <div className="h-full flex flex-col justify-between overflow-hidden rounded-lg border border-[var(--border-default)] bg-[var(--bg-surface)]">
      <div className="overflow-x-auto">
        <table className="w-full text-center border-collapse text-xs sm:text-sm">
          <thead>
            <tr className="bg-slate-50 border-b border-[var(--border-default)] text-[11px] sm:text-xs font-semibold uppercase tracking-wider text-[var(--text-muted)]">
              <th className="py-2 px-2.5 sm:py-2.5 sm:px-4 w-12 sm:w-16 text-center">STT</th>
              <th className="py-2 px-2.5 sm:py-2.5 sm:px-4 text-center">Môn thi</th>
              <th className="py-2 px-2.5 sm:py-2.5 sm:px-4 text-center">Điểm số</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--border-default)]">
            {subjectScores.map((item, index) => (
              <tr
                key={item.code}
                className="md:hover:bg-slate-50/80 transition-colors border-b border-[var(--border-default)] last:border-b-0"
              >
                <td className="py-2 px-2.5 sm:py-2.5 sm:px-4 text-center text-[11px] sm:text-xs text-[var(--text-muted)]">
                  {index + 1}
                </td>
                <td className="py-2 px-2.5 sm:py-2.5 sm:px-4 text-center font-medium text-[var(--text-primary)]">
                  {item.name}
                </td>
                <td className="py-2 px-2.5 sm:py-2.5 sm:px-4 text-center">
                  <span
                    className="text-sm sm:text-base font-bold"
                    style={{ color: item.tier.colorVar }}
                  >
                    {item.tier.displayScore}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {averageTier && summary && (
        <div className="mt-auto bg-slate-50 border-t border-[var(--border-default)] py-2 px-3 sm:py-2.5 sm:px-5 flex items-center justify-between text-[11px] sm:text-xs">
          <span className="font-semibold uppercase tracking-wider text-[var(--text-muted)]">
            Điểm trung bình ({summary.totalTaken} môn dự thi)
          </span>
          <span
            className="text-sm sm:text-base font-bold"
            style={{ color: averageTier.colorVar }}
          >
            {summary.averageScore}
          </span>
        </div>
      )}
    </div>
  )
}

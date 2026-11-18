import { Link } from 'react-router-dom'
import { ExternalLink, Trophy } from 'lucide-react'
import { classifyScoreTier, computeGroupARanking } from '../services/mockData'

function getRankBadgeClass(rank) {
  if (rank === 1) {
    return 'bg-amber-100 text-amber-800 border-amber-300 font-bold'
  }
  if (rank === 2) {
    return 'bg-slate-100 text-slate-700 border-slate-300 font-bold'
  }
  if (rank === 3) {
    return 'bg-orange-100 text-orange-800 border-orange-300 font-bold'
  }
  return 'bg-slate-50 text-[var(--text-muted)] border-[var(--border-default)] font-medium'
}

export default function GroupALeaderboardTable({ topCandidates }) {
  const { rankedCandidates } = computeGroupARanking(topCandidates)

  return (
    <section className="flex-1 flex flex-col min-h-0 bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-4 md:p-5 shadow-2xs">
      <div className="flex items-center gap-2 mb-3">
        <Trophy
          className="h-5 w-5 text-[var(--accent-gold)]"
          strokeWidth={2}
        />
        <h2 className="text-base font-semibold text-[var(--text-primary)]">
          Bảng xếp hạng Top 10 thí sinh Khối A00 (Toán – Vật lí – Hóa học)
        </h2>
      </div>

      <div className="flex-1 min-h-0 overflow-auto rounded-lg border border-[var(--border-default)]">
        <table className="w-full text-center border-collapse text-sm">
          <thead>
            <tr className="bg-slate-50 border-b border-[var(--border-default)] text-xs font-semibold uppercase tracking-wider text-[var(--text-muted)]">
              <th className="py-2.5 px-4 w-24 text-center">Hạng</th>
              <th className="py-2.5 px-4 text-center">Số báo danh</th>
              <th className="py-2.5 px-4 text-center">Toán</th>
              <th className="py-2.5 px-4 text-center">Vật lí</th>
              <th className="py-2.5 px-4 text-center">Hóa học</th>
              <th className="py-2.5 px-4 text-center">Tổng điểm Khối A</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--border-default)]">
            {rankedCandidates.map((item) => {
              const toanTier = classifyScoreTier(item.toan)
              const vatLiTier = classifyScoreTier(item.vatLi)
              const hoaHocTier = classifyScoreTier(item.hoaHoc)
              const totalTier = classifyScoreTier(item.totalScore / 3)

              return (
                <tr
                  key={item.sbd}
                  className="hover:bg-slate-50/80 transition-colors"
                >
                  <td className="py-2 px-4 text-center">
                    <span
                      className={`inline-flex items-center justify-center h-6 w-7 rounded-md text-xs border ${getRankBadgeClass(
                        item.rank,
                      )}`}
                    >
                      {item.rank}
                    </span>
                  </td>

                  <td className="py-2 px-4 text-center font-semibold">
                    <Link
                      to={`/search?sbd=${item.sbd}`}
                      className="inline-flex items-center justify-center gap-1.5 text-[var(--accent-primary)] hover:underline"
                    >
                      <span>{item.sbd}</span>
                      <ExternalLink
                        className="h-3.5 w-3.5 opacity-70"
                        strokeWidth={2}
                      />
                    </Link>
                  </td>

                  <td className="py-2 px-4 text-center">
                    <span
                      className="font-bold"
                      style={{ color: toanTier.colorVar }}
                    >
                      {toanTier.displayScore}
                    </span>
                  </td>

                  <td className="py-2 px-4 text-center">
                    <span
                      className="font-bold"
                      style={{ color: vatLiTier.colorVar }}
                    >
                      {vatLiTier.displayScore}
                    </span>
                  </td>

                  <td className="py-2 px-4 text-center">
                    <span
                      className="font-bold"
                      style={{ color: hoaHocTier.colorVar }}
                    >
                      {hoaHocTier.displayScore}
                    </span>
                  </td>

                  <td className="py-2 px-4 text-center">
                    <span
                      className="text-base font-bold"
                      style={{ color: totalTier.colorVar }}
                    >
                      {Number(item.totalScore).toFixed(2)}
                    </span>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </section>
  )
}

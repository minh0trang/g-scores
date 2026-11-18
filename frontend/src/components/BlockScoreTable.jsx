export default function BlockScoreTable({ blockScores, provinceName }) {
  const bestBlock =
    blockScores && blockScores.length > 0
      ? blockScores.reduce((best, cur) =>
          Number(cur.totalScore) > Number(best.totalScore) ? cur : best,
        )
      : null

  const localHeaderTitle = provinceName
    ? `Hạng tại ${provinceName}`
    : 'Hạng địa phương'

  return (
    <div className="h-full flex flex-col justify-between overflow-hidden rounded-lg border border-[var(--border-default)] bg-[var(--bg-surface)]">
      <div className="overflow-x-auto">
        <table className="w-full text-center border-collapse text-sm">
          <thead>
            <tr className="bg-slate-50 border-b border-[var(--border-default)] text-xs font-semibold uppercase tracking-wider text-[var(--text-muted)]">
              <th className="py-2.5 px-3 w-20 text-center">Khối</th>
              <th className="py-2.5 px-3 text-center">Tổ hợp môn</th>
              <th className="py-2.5 px-3 text-center">Tổng điểm</th>
              <th className="py-2.5 px-3 text-center">Hạng toàn quốc</th>
              <th className="py-2.5 px-3 text-center">{localHeaderTitle}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--border-default)]">
            {blockScores.map((block) => {
              const natRankFormatted =
                block.nationalRank != null
                  ? Number(block.nationalRank).toLocaleString('vi-VN')
                  : '—'
              const natTotalFormatted =
                block.nationalTotal != null
                  ? Number(block.nationalTotal).toLocaleString('vi-VN')
                  : null

              const provRankFormatted =
                block.provincialRank != null
                  ? Number(block.provincialRank).toLocaleString('vi-VN')
                  : '—'
              const provTotalFormatted =
                block.provincialTotal != null
                  ? Number(block.provincialTotal).toLocaleString('vi-VN')
                  : null

              return (
                <tr
                  key={block.code}
                  className="hover:bg-slate-50/80 transition-colors border-b border-[var(--border-default)] last:border-b-0"
                >
                  <td className="py-2.5 px-3 text-center font-semibold text-[var(--text-primary)]">
                    {block.code}
                  </td>

                  <td className="py-2.5 px-3 text-center text-xs text-[var(--text-muted)]">
                    {block.subjectLabel || block.name}
                  </td>

                  <td className="py-2.5 px-3 text-center">
                    <span
                      className="text-base font-bold"
                      style={{ color: block.tier.colorVar }}
                    >
                      {block.totalScore}
                    </span>
                  </td>

                  <td className="py-2.5 px-3 text-center">
                    <div className="inline-flex flex-col items-center">
                      <span className="font-semibold text-[var(--text-primary)]">
                        {natRankFormatted}
                      </span>
                      {natTotalFormatted && (
                        <span className="text-[11px] text-[var(--text-muted)]">
                          / {natTotalFormatted}
                        </span>
                      )}
                    </div>
                  </td>

                  <td className="py-2.5 px-3 text-center">
                    <div className="inline-flex flex-col items-center">
                      <span className="font-semibold text-[var(--accent-primary)]">
                        {provRankFormatted}
                      </span>
                      {provTotalFormatted && (
                        <span className="text-[11px] text-[var(--text-muted)]">
                          / {provTotalFormatted}
                        </span>
                      )}
                    </div>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>

      {bestBlock && (
        <div className="mt-auto bg-slate-50 border-t border-[var(--border-default)] py-2.5 px-4 flex flex-wrap items-center justify-between gap-2 text-xs">
          <span className="font-semibold uppercase tracking-wider text-[var(--text-muted)]">
            Khối điểm cao nhất ({bestBlock.code})
          </span>
          <div className="flex items-center gap-3">
            <span
              className="text-base font-bold"
              style={{ color: bestBlock.tier.colorVar }}
            >
              {bestBlock.totalScore}
            </span>
            {bestBlock.nationalRank != null && (
              <span className="text-[var(--text-muted)]">
                Toàn quốc: #{Number(bestBlock.nationalRank).toLocaleString('vi-VN')}
              </span>
            )}
            {bestBlock.provincialRank != null && (
              <span className="text-[var(--accent-primary)] font-medium">
                Tỉnh: #{Number(bestBlock.provincialRank).toLocaleString('vi-VN')}
              </span>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

import { Link } from 'react-router-dom'
import { Award, Crown, ExternalLink, Medal, Trophy } from 'lucide-react'
import ProvinceSelector from './ProvinceSelector'
import { computeGroupARanking } from '../services/mockData'

function renderRankBadge(rank) {
  if (rank === 1) {
    return (
      <span className="inline-flex items-center justify-center gap-1 h-6 px-2 rounded-md text-xs font-black bg-amber-400 text-amber-950 border border-amber-500 shadow-2xs">
        <Crown className="h-3 w-3 fill-amber-950 text-amber-950" />
        <span>1</span>
      </span>
    )
  }
  if (rank === 2) {
    return (
      <span className="inline-flex items-center justify-center gap-1 h-6 px-2 rounded-md text-xs font-bold bg-slate-200 text-slate-800 border border-slate-300 shadow-2xs">
        <Medal className="h-3 w-3 text-slate-600" />
        <span>2</span>
      </span>
    )
  }
  if (rank === 3) {
    return (
      <span className="inline-flex items-center justify-center gap-1 h-6 px-2 rounded-md text-xs font-bold bg-orange-100 text-orange-900 border border-orange-300 shadow-2xs">
        <Award className="h-3 w-3 text-orange-700" />
        <span>3</span>
      </span>
    )
  }
  return (
    <span className="inline-flex items-center justify-center h-6 w-6 rounded-md text-xs font-semibold bg-slate-100 text-slate-600 border border-slate-200">
      {rank}
    </span>
  )
}

function getRowClass(rank) {
  if (rank === 1) {
    return 'bg-amber-50/40 hover:bg-amber-50/70 border-b border-amber-100 transition-colors'
  }
  if (rank === 2) {
    return 'bg-slate-50/60 hover:bg-slate-100/70 border-b border-slate-200 transition-colors'
  }
  if (rank === 3) {
    return 'bg-orange-50/30 hover:bg-orange-50/60 border-b border-orange-100 transition-colors'
  }
  return 'hover:bg-slate-50/80 border-b border-[var(--border-default)] transition-colors'
}

function getSbdClass(rank) {
  if (rank === 1) {
    return 'inline-flex items-center justify-center gap-1.5 font-bold text-amber-950 hover:text-amber-800 hover:underline'
  }
  if (rank === 2) {
    return 'inline-flex items-center justify-center gap-1.5 font-bold text-slate-900 hover:text-slate-700 hover:underline'
  }
  if (rank === 3) {
    return 'inline-flex items-center justify-center gap-1.5 font-bold text-orange-950 hover:text-orange-800 hover:underline'
  }
  return 'inline-flex items-center justify-center gap-1.5 font-semibold text-[var(--accent-primary)] hover:underline'
}

function renderSubjectScore(score) {
  const num = Number(score) || 0
  const formatted = num.toFixed(2)
  if (num >= 10) {
    return (
      <span className="inline-block px-1.5 py-0.5 rounded bg-emerald-50 text-emerald-800 font-extrabold border border-emerald-300 text-xs shadow-2xs">
        {formatted}
      </span>
    )
  }
  if (num >= 9.5) {
    return <span className="font-bold text-slate-800">{formatted}</span>
  }
  return <span className="font-medium text-slate-600">{formatted}</span>
}

function renderTotalScore(rank, totalScore) {
  const formatted = (Number(totalScore) || 0).toFixed(2)
  if (rank === 1) {
    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-sm font-black bg-amber-100 text-amber-900 border border-amber-400 shadow-2xs">
        {formatted}
      </span>
    )
  }
  if (rank === 2) {
    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-sm font-bold bg-slate-100 text-slate-800 border border-slate-300 shadow-2xs">
        {formatted}
      </span>
    )
  }
  if (rank === 3) {
    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-sm font-bold bg-orange-100 text-orange-900 border border-orange-300 shadow-2xs">
        {formatted}
      </span>
    )
  }
  return (
    <span className="inline-flex items-center px-2 py-0.5 rounded-md text-sm font-bold bg-emerald-50 text-emerald-800 border border-emerald-200">
      {formatted}
    </span>
  )
}

export default function GroupALeaderboardTable({
  topCandidates,
  provinces,
  selectedProvince,
  onSelectProvince,
}) {
  const { rankedCandidates } = computeGroupARanking(topCandidates)

  return (
    <section className="flex-1 flex flex-col min-h-0 bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-4 md:p-5 shadow-2xs">
      <div className="flex flex-wrap items-center justify-between gap-3 mb-3">
        <div className="flex items-center gap-2">
          <Trophy
            className="h-5 w-5 text-[var(--accent-gold)] shrink-0"
            strokeWidth={2}
          />
          <h2 className="text-sm md:text-base font-semibold text-[var(--text-primary)]">
            Bảng xếp hạng Top 10 thí sinh Khối A00 (Toán – Vật lí – Hóa học)
          </h2>
        </div>

        {provinces && onSelectProvince && (
          <ProvinceSelector
            provinces={provinces}
            selectedProvince={selectedProvince}
            onSelectProvince={onSelectProvince}
          />
        )}
      </div>

      <div className="flex-1 min-h-0 overflow-auto rounded-lg border border-[var(--border-default)]">
        <table className="w-full text-center border-collapse text-sm">
          <thead>
            <tr className="bg-slate-50 border-b border-[var(--border-default)] text-xs font-semibold uppercase tracking-wider text-[var(--text-muted)]">
              <th className="py-2.5 px-3 w-20 text-center">Hạng</th>
              <th className="py-2.5 px-3 text-center">Số báo danh</th>
              <th className="py-2.5 px-3 text-center">Địa phương</th>
              <th className="py-2.5 px-3 text-center">Toán</th>
              <th className="py-2.5 px-3 text-center">Vật lí</th>
              <th className="py-2.5 px-3 text-center">Hóa học</th>
              <th className="py-2.5 px-3 text-center">Tổng điểm Khối A</th>
            </tr>
          </thead>
          <tbody>
            {rankedCandidates.map((item) => {
              return (
                <tr key={item.sbd} className={getRowClass(item.rank)}>
                  <td className="py-2 px-3 text-center">
                    {renderRankBadge(item.rank)}
                  </td>

                  <td className="py-2 px-3 text-center">
                    <Link
                      to={`/search?sbd=${item.sbd}`}
                      className={getSbdClass(item.rank)}
                    >
                      <span>{item.sbd}</span>
                      <ExternalLink
                        className="h-3.5 w-3.5 opacity-70"
                        strokeWidth={2}
                      />
                    </Link>
                  </td>

                  <td className="py-2 px-3 text-center text-xs font-medium text-[var(--text-primary)]">
                    {item.provinceName || '—'}
                  </td>

                  <td className="py-2 px-3 text-center">
                    {renderSubjectScore(item.toan)}
                  </td>

                  <td className="py-2 px-3 text-center">
                    {renderSubjectScore(item.vatLi)}
                  </td>

                  <td className="py-2 px-3 text-center">
                    {renderSubjectScore(item.hoaHoc)}
                  </td>

                  <td className="py-2 px-3 text-center">
                    {renderTotalScore(item.rank, item.totalScore)}
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

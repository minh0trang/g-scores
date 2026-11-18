import { Award, Medal, Trophy } from 'lucide-react'
import { computeGroupARanking } from '../services/mockData'

const PODIUM_STYLES = [
  {
    rank: 1,
    icon: Trophy,
    badgeBg: 'bg-amber-100 text-amber-800 border-amber-300',
    iconColor: 'text-[var(--accent-gold)]',
    cardBorder: 'border-amber-300 bg-amber-50/30',
  },
  {
    rank: 2,
    icon: Medal,
    badgeBg: 'bg-slate-100 text-slate-700 border-slate-300',
    iconColor: 'text-slate-500',
    cardBorder: 'border-[var(--border-default)] bg-[var(--bg-surface)]',
  },
  {
    rank: 3,
    icon: Award,
    badgeBg: 'bg-orange-100 text-orange-800 border-orange-300',
    iconColor: 'text-orange-600',
    cardBorder: 'border-[var(--border-default)] bg-[var(--bg-surface)]',
  },
]

export default function TopThreeGroupACards({ topCandidates }) {
  const { topThreeGroups } = computeGroupARanking(topCandidates)

  return (
    <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 sm:gap-3.5">
      {topThreeGroups.map((group, index) => {
        const meta = PODIUM_STYLES[index] || PODIUM_STYLES[2]
        const IconComponent = meta.icon

        return (
          <div
            key={group.scoreKey}
            className={`rounded-xl border p-2.5 sm:p-4 shadow-2xs flex items-center justify-between gap-2 ${meta.cardBorder}`}
          >
            <span
              className={`inline-flex items-center gap-1 sm:gap-1.5 px-2 py-0.5 sm:px-2.5 sm:py-1 rounded-md text-[11px] sm:text-xs font-semibold border ${meta.badgeBg}`}
            >
              <IconComponent
                className={`h-3.5 w-3.5 sm:h-4 sm:w-4 ${meta.iconColor}`}
                strokeWidth={2}
              />
              <span>{group.roleLabel}</span>
            </span>

            <span
              className="text-lg sm:text-xl font-bold"
              style={{ color: 'var(--state-success)' }}
            >
              {group.scoreKey}
            </span>
          </div>
        )
      })}
    </div>
  )
}

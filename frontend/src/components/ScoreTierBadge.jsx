import { classifyScoreTier } from '../services/mockData'

export default function ScoreTierBadge({ score, tier: providedTier }) {
  const tier = providedTier || classifyScoreTier(score)

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-medium border ${tier.badgeClass}`}
    >
      <span
        className="h-2 w-2 rounded-full shrink-0"
        style={{ backgroundColor: tier.colorVar }}
      />
      <span>{tier.label}</span>
    </span>
  )
}


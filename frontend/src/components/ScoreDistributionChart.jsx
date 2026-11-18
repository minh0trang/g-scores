import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { BarChart3 } from 'lucide-react'

const TIER_SERIES = [
  {
    dataKey: 'excellent',
    name: 'Giỏi (>= 8.0)',
    color: 'var(--state-success)',
  },
  {
    dataKey: 'good',
    name: 'Khá (6.0 – <8.0)',
    color: 'var(--state-good)',
  },
  {
    dataKey: 'average',
    name: 'Trung bình (4.0 – <6.0)',
    color: 'var(--state-warning)',
  },
  {
    dataKey: 'low',
    name: 'Yếu / Kém (< 4.0)',
    color: 'var(--state-error)',
  },
]

function formatCompactNumber(value) {
  const num = Number(value) || 0
  if (num >= 1000000) return `${(num / 1000000).toFixed(1)}M`
  if (num >= 1000) return `${Math.round(num / 1000)}K`
  return String(num)
}

export default function ScoreDistributionChart({
  distribution,
  selectedSubjectCode,
  selectedSubjectData,
}) {
  const isSingleSubject = selectedSubjectCode !== 'ALL' && selectedSubjectData

  const singleSubjectBars = isSingleSubject
    ? [
        {
          tierName: 'Giỏi (>= 8.0)',
          count: selectedSubjectData.excellent,
          color: 'var(--state-success)',
        },
        {
          tierName: 'Khá (6.0 – <8.0)',
          count: selectedSubjectData.good,
          color: 'var(--state-good)',
        },
        {
          tierName: 'Trung bình (4.0 – <6.0)',
          count: selectedSubjectData.average,
          color: 'var(--state-warning)',
        },
        {
          tierName: 'Yếu / Kém (< 4.0)',
          count: selectedSubjectData.low,
          color: 'var(--state-error)',
        },
      ]
    : []

  return (
    <section className="flex-1 flex flex-col min-h-0 bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-4 md:p-5 shadow-2xs overflow-hidden">
      <div className="flex items-center gap-2 mb-4">
        <BarChart3
          className="h-5 w-5 text-[var(--accent-primary)]"
          strokeWidth={2}
        />
        <h2 className="text-base font-semibold text-[var(--text-primary)]">
          {isSingleSubject
            ? `Biểu đồ phổ điểm môn ${selectedSubjectData.subjectName}`
            : 'Biểu đồ phổ điểm 4 mức theo môn học'}
        </h2>
      </div>

      <div className="flex-1 min-h-[260px] w-full relative overflow-hidden">
        <ResponsiveContainer width="100%" height="100%">
          {isSingleSubject ? (
            <BarChart
              data={singleSubjectBars}
              margin={{ top: 8, right: 16, left: 4, bottom: 4 }}
            >
              <CartesianGrid
                strokeDasharray="3 3"
                vertical={false}
                stroke="var(--border-default)"
              />
              <XAxis
                dataKey="tierName"
                tick={{ fill: 'var(--text-primary)', fontSize: 12 }}
                axisLine={{ stroke: 'var(--border-default)' }}
                tickLine={false}
              />
              <YAxis
                tickFormatter={formatCompactNumber}
                tick={{ fill: 'var(--text-muted)', fontSize: 12 }}
                axisLine={false}
                tickLine={false}
              />
              <Tooltip
                isAnimationActive={false}
                wrapperStyle={{ pointerEvents: 'none', zIndex: 50 }}
                formatter={(value) => [
                  `${Number(value).toLocaleString('vi-VN')} thí sinh`,
                  'Số lượng',
                ]}
                contentStyle={{
                  backgroundColor: 'var(--bg-surface)',
                  borderColor: 'var(--border-default)',
                  borderRadius: '0.5rem',
                  fontSize: '0.8125rem',
                  boxShadow:
                    '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
                }}
              />
              <Bar
                dataKey="count"
                radius={[6, 6, 0, 0]}
                maxBarSize={68}
                isAnimationActive={false}
              >
                {singleSubjectBars.map((entry) => (
                  <Cell key={entry.tierName} fill={entry.color} />
                ))}
              </Bar>
            </BarChart>
          ) : (
            <BarChart
              data={distribution}
              margin={{ top: 8, right: 16, left: 4, bottom: 4 }}
            >
              <CartesianGrid
                strokeDasharray="3 3"
                vertical={false}
                stroke="var(--border-default)"
              />
              <XAxis
                dataKey="subjectName"
                tick={{ fill: 'var(--text-primary)', fontSize: 12 }}
                axisLine={{ stroke: 'var(--border-default)' }}
                tickLine={false}
              />
              <YAxis
                tickFormatter={formatCompactNumber}
                tick={{ fill: 'var(--text-muted)', fontSize: 12 }}
                axisLine={false}
                tickLine={false}
              />
              <Tooltip
                isAnimationActive={false}
                wrapperStyle={{ pointerEvents: 'none', zIndex: 50 }}
                formatter={(value, name) => [
                  `${Number(value).toLocaleString('vi-VN')} thí sinh`,
                  name,
                ]}
                contentStyle={{
                  backgroundColor: 'var(--bg-surface)',
                  borderColor: 'var(--border-default)',
                  borderRadius: '0.5rem',
                  fontSize: '0.8125rem',
                  boxShadow:
                    '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
                }}
              />
              <Legend
                wrapperStyle={{ fontSize: '0.75rem', paddingTop: '8px' }}
              />
              {TIER_SERIES.map((series) => (
                <Bar
                  key={series.dataKey}
                  dataKey={series.dataKey}
                  name={series.name}
                  fill={series.color}
                  radius={[4, 4, 0, 0]}
                  isAnimationActive={false}
                />
              ))}
            </BarChart>
          )}
        </ResponsiveContainer>
      </div>
    </section>
  )
}

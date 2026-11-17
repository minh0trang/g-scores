import { BarChart3, Trophy } from 'lucide-react'

export default function Leaderboard() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-[var(--text-primary)]">
          Báo cáo & Bảng xếp hạng
        </h1>
        <p className="text-sm text-[var(--text-muted)] mt-1">
          Thống kê chi tiết số lượng thí sinh theo 4 mức điểm ở từng môn và danh sách Top 10 thí sinh Khối A.
        </p>
      </div>

      {/* Score Statistics Chart Placeholder */}
      <section className="bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-6 shadow-2xs">
        <div className="flex items-center gap-2.5 mb-3">
          <BarChart3 className="h-5 w-5 text-[var(--accent-primary)]" strokeWidth={2} />
          <h2 className="text-lg font-semibold text-[var(--text-primary)]">
            Thống kê phổ điểm theo môn (4 mức điểm)
          </h2>
        </div>
        <p className="text-sm text-[var(--text-muted)]">
          Biểu đồ cột trực quan (Recharts) sẽ được tích hợp ở Unit 4.
        </p>
      </section>

      {/* Group A Leaderboard Placeholder */}
      <section className="bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-6 shadow-2xs">
        <div className="flex items-center gap-2.5 mb-3">
          <Trophy className="h-5 w-5 text-[var(--accent-gold)]" strokeWidth={2} />
          <h2 className="text-lg font-semibold text-[var(--text-primary)]">
            Top 10 thí sinh Khối A (Toán, Vật lí, Hóa học)
          </h2>
        </div>
        <p className="text-sm text-[var(--text-muted)]">
          Bảng xếp hạng 10 thí sinh điểm cao nhất Khối A sẽ được tích hợp ở Unit 4.
        </p>
      </section>
    </div>
  )
}

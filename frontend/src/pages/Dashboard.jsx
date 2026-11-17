import { Link } from 'react-router-dom'
import { BarChart3, BookOpen, Search, Trophy, Users } from 'lucide-react'

export default function Dashboard() {
  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-[var(--text-primary)]">
            Tổng quan kỳ thi
          </h1>
          <p className="text-sm text-[var(--text-muted)] mt-1">
            Thống kê tổng quan Kỳ thi Tốt nghiệp THPT 2024, biểu đồ phân bố phổ điểm và Bảng xếp hạng Top 10 Khối A.
          </p>
        </div>

        <Link
          to="/search"
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-lg bg-[var(--accent-primary)] text-white text-sm font-medium hover:bg-blue-700 transition-colors shadow-xs self-start sm:self-auto"
        >
          <Search className="h-4 w-4" strokeWidth={2} />
          <span>Tra cứu theo SBD</span>
        </Link>
      </div>

      {/* KPI Summary Skeleton Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-5 shadow-2xs flex items-center gap-4">
          <div className="h-12 w-12 rounded-xl bg-blue-50 text-[var(--accent-primary)] flex items-center justify-center shrink-0">
            <Users className="h-5 w-5" strokeWidth={2} />
          </div>
          <div>
            <p className="text-xs font-medium uppercase tracking-wider text-[var(--text-muted)]">
              Tổng số thí sinh
            </p>
            <p className="text-2xl font-bold font-mono text-[var(--text-primary)] mt-0.5">
              1,061,605
            </p>
          </div>
        </div>

        <div className="bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-5 shadow-2xs flex items-center gap-4">
          <div className="h-12 w-12 rounded-xl bg-emerald-50 text-[var(--state-success)] flex items-center justify-center shrink-0">
            <BookOpen className="h-5 w-5" strokeWidth={2} />
          </div>
          <div>
            <p className="text-xs font-medium uppercase tracking-wider text-[var(--text-muted)]">
              Số môn thi
            </p>
            <p className="text-2xl font-bold font-mono text-[var(--text-primary)] mt-0.5">
              9
            </p>
          </div>
        </div>

        <div className="bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-5 shadow-2xs flex items-center gap-4">
          <div className="h-12 w-12 rounded-xl bg-amber-50 text-[var(--accent-gold)] flex items-center justify-center shrink-0">
            <Trophy className="h-5 w-5" strokeWidth={2} />
          </div>
          <div>
            <p className="text-xs font-medium uppercase tracking-wider text-[var(--text-muted)]">
              Thủ khoa Khối A
            </p>
            <p className="text-2xl font-bold font-mono text-[var(--text-primary)] mt-0.5">
              29.60
            </p>
          </div>
        </div>
      </div>

      {/* Placeholder Sections for Upcoming Units */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-6 shadow-2xs">
          <div className="flex items-center gap-2.5 mb-2">
            <BarChart3 className="h-5 w-5 text-[var(--accent-primary)]" strokeWidth={2} />
            <h2 className="text-base font-semibold text-[var(--text-primary)]">
              Phổ điểm theo 4 mức độ
            </h2>
          </div>
          <p className="text-sm text-[var(--text-muted)]">
            Biểu đồ thống kê 4 mức điểm theo từng môn thi sẽ được hiển thị tại đây ở Unit 4.
          </p>
        </div>

        <div className="bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-6 shadow-2xs">
          <div className="flex items-center gap-2.5 mb-2">
            <Trophy className="h-5 w-5 text-[var(--accent-gold)]" strokeWidth={2} />
            <h2 className="text-base font-semibold text-[var(--text-primary)]">
              Bảng xếp hạng Top 10 Khối A
            </h2>
          </div>
          <p className="text-sm text-[var(--text-muted)]">
            Danh sách 10 thí sinh có tổng điểm Toán + Vật lí + Hóa học cao nhất sẽ hiển thị tại đây ở Unit 4.
          </p>
        </div>
      </div>
    </div>
  )
}

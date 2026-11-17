import { FileText, Search } from 'lucide-react'

export default function ScoreLookup() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-[var(--text-primary)]">
          Tra cứu điểm thi
        </h1>
        <p className="text-sm text-[var(--text-muted)] mt-1">
          Nhập Số báo danh (SBD gồm 8 chữ số) để xem chi tiết kết quả điểm thi của thí sinh ở cả 9 môn.
        </p>
      </div>

      {/* User Registration Card Placeholder */}
      <section className="bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-6 shadow-2xs">
        <div className="flex items-center gap-2.5 mb-3">
          <Search className="h-5 w-5 text-[var(--accent-primary)]" strokeWidth={2} />
          <h2 className="text-lg font-semibold text-[var(--text-primary)]">
            Thông tin đăng ký dự thi
          </h2>
        </div>
        <p className="text-sm text-[var(--text-muted)]">
          Biểu mẫu nhập Số báo danh và kiểm tra định dạng sẽ được kết nối ở Unit 3.
        </p>
      </section>

      {/* Detailed Scores Card Placeholder */}
      <section className="bg-[var(--bg-surface)] border border-[var(--border-default)] rounded-xl p-6 shadow-2xs">
        <div className="flex items-center gap-2.5 mb-3">
          <FileText className="h-5 w-5 text-[var(--accent-primary)]" strokeWidth={2} />
          <h2 className="text-lg font-semibold text-[var(--text-primary)]">
            Bảng điểm chi tiết
          </h2>
        </div>
        <p className="text-sm text-[var(--text-muted)]">
          Kết quả 9 môn thi kèm nhãn phân loại mức điểm sẽ được hiển thị tại đây ở Unit 3.
        </p>
      </section>
    </div>
  )
}

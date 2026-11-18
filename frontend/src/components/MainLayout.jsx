import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import {
  GraduationCap,
  LayoutDashboard,
  Menu,
  Search,
  Trophy,
  X,
} from 'lucide-react'

const NAV_ITEMS = [
  {
    to: '/',
    label: 'Tổng quan',
    icon: LayoutDashboard,
    end: true,
  },
  {
    to: '/search',
    label: 'Tra cứu điểm',
    icon: Search,
    end: false,
  },
  {
    to: '/reports',
    label: 'Xếp hạng Khối A',
    icon: Trophy,
    end: false,
  },
]

export default function MainLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const closeSidebar = () => setSidebarOpen(false)

  return (
    <div className="min-h-screen lg:h-screen lg:overflow-hidden flex flex-col bg-[var(--bg-base)] text-[var(--text-primary)]">
      {/* Top Fixed Brand Header */}
      <header className="sticky top-0 z-30 h-14 sm:h-16 shrink-0 bg-[var(--bg-brand)] text-white shadow-md">
        <div className="h-full px-3.5 sm:px-4 md:px-6 flex items-center justify-between">
          <div className="flex items-center gap-2.5 sm:gap-3">
            <button
              type="button"
              onClick={() => setSidebarOpen((prev) => !prev)}
              aria-label="Mở hoặc đóng thanh điều hướng"
              className="lg:hidden p-1.5 sm:p-2 rounded-lg text-white/90 hover:text-white hover:bg-white/10 transition-colors cursor-pointer"
            >
              {sidebarOpen ? (
                <X className="h-5 w-5" strokeWidth={2} />
              ) : (
                <Menu className="h-5 w-5" strokeWidth={2} />
              )}
            </button>

            <div className="flex items-center gap-2 sm:gap-2.5">
              <div className="h-8 w-8 sm:h-9 sm:w-9 rounded-lg bg-[var(--accent-gold)] text-slate-950 flex items-center justify-center shadow-sm">
                <GraduationCap className="h-4.5 w-4.5 sm:h-5 sm:w-5" strokeWidth={2} />
              </div>
              <div>
                <span className="text-base sm:text-lg font-bold tracking-tight block leading-none">
                  G-Scores
                </span>
                <span className="text-[11px] sm:text-xs text-blue-200 block mt-0.5">
                  Tra cứu & Phân tích Điểm thi THPT 2024
                </span>
              </div>
            </div>
          </div>
        </div>
      </header>

      <div className="flex-1 flex relative min-h-0">
        {/* Mobile Slide-Over Backdrop */}
        {sidebarOpen && (
          <div
            className="fixed inset-0 z-20 bg-slate-900/50 backdrop-blur-xs lg:hidden"
            onClick={closeSidebar}
            aria-hidden="true"
          />
        )}

        {/* Left Navigation Sidebar */}
        <aside
          className={`fixed lg:static inset-y-0 left-0 z-20 w-64 bg-[var(--bg-surface)] border-r border-[var(--border-default)] pt-16 lg:pt-0 flex flex-col justify-between transition-transform duration-200 ease-in-out ${
            sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
          }`}
        >
          <div className="p-4">
            <div className="px-3 mb-2 text-xs font-semibold uppercase tracking-wider text-[var(--text-muted)]">
              Danh mục
            </div>
            <nav className="space-y-1.5" aria-label="Điều hướng chính">
              {NAV_ITEMS.map((item) => {
                const IconComponent = item.icon
                return (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    end={item.end}
                    onClick={closeSidebar}
                    className={({ isActive }) =>
                      `group flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium transition-all ${
                        isActive
                          ? 'bg-[var(--bg-brand)] text-white shadow-xs'
                          : 'text-[var(--text-primary)] hover:bg-slate-100'
                      }`
                    }
                  >
                    {({ isActive }) => (
                      <>
                        <IconComponent
                          className={`h-5 w-5 shrink-0 ${
                            isActive
                              ? 'text-[var(--accent-gold)]'
                              : 'text-[var(--text-muted)] group-hover:text-[var(--text-primary)]'
                          }`}
                          strokeWidth={2}
                        />
                        <span>{item.label}</span>
                      </>
                    )}
                  </NavLink>
                )
              })}
            </nav>
          </div>
        </aside>

        {/* Main Content Area */}
        <main className="flex-1 min-w-0 min-h-0 flex flex-col overflow-y-auto">
          <div className="w-full max-w-7xl mx-auto p-2.5 sm:p-4 md:p-5 flex-1 flex flex-col min-h-0">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  )
}

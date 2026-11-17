import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import MainLayout from './components/MainLayout'
import Dashboard from './pages/Dashboard'
import Leaderboard from './pages/Leaderboard'
import ScoreLookup from './pages/ScoreLookup'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<MainLayout />}>
          <Route index element={<Dashboard />} />
          <Route path="search" element={<ScoreLookup />} />
          <Route path="reports" element={<Leaderboard />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

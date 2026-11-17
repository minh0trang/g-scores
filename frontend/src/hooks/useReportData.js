import { useCallback, useEffect, useState } from 'react'
import {
  fetchOverviewStats,
  fetchScoreDistribution,
  fetchTop10GroupA,
} from '../services/gscoresService'

export function useReportData() {
  const [overview, setOverview] = useState(null)
  const [scoreDistribution, setScoreDistribution] = useState([])
  const [top10GroupA, setTop10GroupA] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [reloadTick, setReloadTick] = useState(0)

  useEffect(() => {
    let isCancelled = false

    async function fetchAllReports() {
      try {
        const [overviewData, distributionData, top10Data] = await Promise.all([
          fetchOverviewStats(),
          fetchScoreDistribution(),
          fetchTop10GroupA(),
        ])

        if (!isCancelled) {
          setOverview(overviewData)
          setScoreDistribution(distributionData)
          setTop10GroupA(top10Data)
          setError(null)
        }
      } catch (err) {
        if (!isCancelled) {
          setError(
            err?.message ||
              'Không thể tải dữ liệu báo cáo thống kê. Vui lòng thử lại.',
          )
        }
      } finally {
        if (!isCancelled) {
          setLoading(false)
        }
      }
    }

    fetchAllReports()

    return () => {
      isCancelled = true
    }
  }, [reloadTick])

  const refreshReports = useCallback(() => {
    setLoading(true)
    setError(null)
    setReloadTick((prev) => prev + 1)
  }, [])

  return {
    overview,
    scoreDistribution,
    top10GroupA,
    loading,
    error,
    refreshReports,
  }
}

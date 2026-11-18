import { useCallback, useEffect, useState } from 'react'
import {
  fetchOverviewStats,
  fetchScoreDistribution,
  fetchTop10GroupA,
} from '../services/gscoresService'

let cachedOverview = null
let cachedDistribution = null
let cachedTop10GroupA = null
let inFlightPromise = null

export function useReportData() {
  const hasCache = cachedDistribution !== null && cachedTop10GroupA !== null

  const [overview, setOverview] = useState(cachedOverview)
  const [scoreDistribution, setScoreDistribution] = useState(
    cachedDistribution || [],
  )
  const [top10GroupA, setTop10GroupA] = useState(cachedTop10GroupA || [])
  const [loading, setLoading] = useState(!hasCache)
  const [error, setError] = useState(null)
  const [reloadTick, setReloadTick] = useState(0)

  useEffect(() => {
    if (
      reloadTick === 0 &&
      cachedDistribution !== null &&
      cachedTop10GroupA !== null
    ) {
      return
    }

    let isCancelled = false

    async function fetchAllReports() {
      try {
        if (!inFlightPromise || reloadTick > 0) {
          inFlightPromise = Promise.all([
            fetchOverviewStats(),
            fetchScoreDistribution(),
            fetchTop10GroupA(),
          ])
        }

        const [overviewData, distributionData, top10Data] =
          await inFlightPromise

        cachedOverview = overviewData
        cachedDistribution = distributionData
        cachedTop10GroupA = top10Data

        if (!isCancelled) {
          setOverview(overviewData)
          setScoreDistribution(distributionData)
          setTop10GroupA(top10Data)
          setError(null)
        }
      } catch (err) {
        inFlightPromise = null
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
    cachedOverview = null
    cachedDistribution = null
    cachedTop10GroupA = null
    inFlightPromise = null
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

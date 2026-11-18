import { useCallback, useEffect, useState } from 'react'
import {
  fetchOverviewStats,
  fetchProvinces,
  fetchScoreDistribution,
  fetchTop10GroupA,
} from '../services/gscoresService'

let cachedProvincesList = null
const reportCache = new Map()
const inFlightRequests = new Map()

export function useReportData(initialProvince = '') {
  const [selectedProvince, setSelectedProvince] = useState(initialProvince)
  const [provinces, setProvinces] = useState(cachedProvincesList || [])

  const currentKey = selectedProvince || 'ALL'
  const cachedData = reportCache.get(currentKey)

  const [loading, setLoading] = useState(!cachedData)
  const [error, setError] = useState(null)
  const [reloadTick, setReloadTick] = useState(0)
  const [fetchedData, setFetchedData] = useState(null)

  const activeData = cachedData || fetchedData
  const overview = activeData?.overview || null
  const scoreDistribution = activeData?.scoreDistribution || []
  const top10GroupA = activeData?.top10GroupA || []

  useEffect(() => {
    let isCancelled = false
    const key = selectedProvince || 'ALL'

    // Load provinces once if not yet cached
    if (!cachedProvincesList) {
      fetchProvinces()
        .then((data) => {
          cachedProvincesList = data
          if (!isCancelled) {
            setProvinces(data)
          }
        })
        .catch((err) => {
          console.warn('Failed to load provinces list:', err)
        })
    }

    if (reloadTick === 0 && reportCache.has(key)) {
      return
    }

    async function loadDataForProvince() {
      try {
        let requestPromise = inFlightRequests.get(key)
        if (!requestPromise || reloadTick > 0) {
          requestPromise = Promise.all([
            fetchOverviewStats(selectedProvince || undefined),
            fetchScoreDistribution(selectedProvince || undefined),
            fetchTop10GroupA(selectedProvince || undefined),
          ])
          inFlightRequests.set(key, requestPromise)
        }

        const [overviewData, distributionData, top10Data] =
          await requestPromise

        const result = {
          overview: overviewData,
          scoreDistribution: distributionData,
          top10GroupA: top10Data,
        }
        reportCache.set(key, result)
        inFlightRequests.delete(key)

        if (!isCancelled) {
          setFetchedData(result)
          setError(null)
        }
      } catch (err) {
        inFlightRequests.delete(key)
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

    loadDataForProvince()

    return () => {
      isCancelled = true
    }
  }, [selectedProvince, reloadTick])

  const handleSelectProvince = useCallback((provinceCode) => {
    const key = provinceCode || 'ALL'
    setSelectedProvince(provinceCode)
    setError(null)
    if (!reportCache.has(key)) {
      setLoading(true)
    } else {
      setLoading(false)
    }
  }, [])

  const refreshReports = useCallback(() => {
    const key = selectedProvince || 'ALL'
    reportCache.delete(key)
    inFlightRequests.delete(key)
    setLoading(true)
    setError(null)
    setReloadTick((prev) => prev + 1)
  }, [selectedProvince])

  return {
    overview,
    scoreDistribution,
    top10GroupA,
    provinces,
    selectedProvince,
    setSelectedProvince: handleSelectProvince,
    loading,
    error,
    refreshReports,
  }
}

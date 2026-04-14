import { request } from "@/http/axios"

export interface OverviewData {
  totalUsers: number
  totalInfos: number
  pendingInfos: number
  publishedInfos: number
  finishedInfos: number
  rejectedInfos: number
  totalNotices: number
}

export interface TrendItem {
  date: string
  count: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

/** 总览统计 */
export function getOverview() {
  return request<ApiResponse<OverviewData>>({
    url: "/admin/statistics/overview",
    method: "get"
  })
}

/** 近7天趋势 */
export function getRecent7DaysTrend() {
  return request<ApiResponse<{ list: TrendItem[] }>>({
    url: "/admin/statistics/trend/recent7days",
    method: "get"
  })
}

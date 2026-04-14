import { request } from "@/http/axios"

export interface LostFoundQuery {
  pageNum: number
  pageSize: number
  type?: number
  status?: number
  keyword?: string
}

export interface LostFoundItem {
  id: number
  type: number
  title: string
  itemName: string
  categoryId: number
  image: string
  eventPlace: string
  eventTime: string
  contactName: string
  contactPhone: string
  status: number
  viewCount: number
  createTime: string
}

export interface LostFoundDetail extends LostFoundItem {
  userId: number
  brand: string
  color: string
  description: string
  contactWechat: string
  auditReason: string | null
  auditAdminId: number | null
  auditTime: string | null
  finishTime: string | null
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

/** 待审核列表 */
export function getPendingList(params: LostFoundQuery) {
  return request<ApiResponse<PageResult<LostFoundItem>>>({
    url: "/admin/lostfound/pending",
    method: "get",
    params
  })
}

/** 全部信息列表 */
export function getLostFoundList(params: LostFoundQuery) {
  return request<ApiResponse<PageResult<LostFoundItem>>>({
    url: "/admin/lostfound/list",
    method: "get",
    params
  })
}

/** 信息详情 */
export function getLostFoundDetail(id: number) {
  return request<ApiResponse<LostFoundDetail>>({
    url: `/admin/lostfound/${id}`,
    method: "get"
  })
}

/** 审核通过 */
export function approveLostFound(id: number) {
  return request<ApiResponse<null>>({
    url: `/admin/lostfound/${id}/approve`,
    method: "put"
  })
}

/** 审核驳回 */
export function rejectLostFound(id: number, auditReason: string) {
  return request<ApiResponse<null>>({
    url: `/admin/lostfound/${id}/reject`,
    method: "put",
    data: { auditReason }
  })
}

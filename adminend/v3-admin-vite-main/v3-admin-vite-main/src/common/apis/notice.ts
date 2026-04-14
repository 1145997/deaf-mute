import { request } from "@/http/axios"

export interface NoticeQuery {
  pageNum: number
  pageSize: number
}

export interface NoticeItem {
  id: number
  title: string
  content: string
  isTop: number
  status: number
  publishAdminId: number
  createTime: string
  updateTime: string
}

export interface NoticeFormData {
  title: string
  content: string
  isTop: number
  status: number
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

/** 公告列表 */
export function getNoticeList(params: NoticeQuery) {
  return request<ApiResponse<PageResult<NoticeItem>>>({
    url: "/admin/notice/list",
    method: "get",
    params
  })
}

/** 新增公告 */
export function createNotice(data: NoticeFormData) {
  return request<ApiResponse<null>>({
    url: "/admin/notice",
    method: "post",
    data
  })
}

/** 修改公告 */
export function updateNotice(id: number, data: NoticeFormData) {
  return request<ApiResponse<null>>({
    url: `/admin/notice/${id}`,
    method: "put",
    data
  })
}

/** 删除公告 */
export function deleteNotice(id: number) {
  return request<ApiResponse<null>>({
    url: `/admin/notice/${id}`,
    method: "delete"
  })
}

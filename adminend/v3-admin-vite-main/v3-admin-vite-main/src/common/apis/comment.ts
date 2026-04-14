import { request } from "@/http/axios"

export interface CommentQuery {
  pageNum: number
  pageSize: number
  infoId?: number
  status?: number
  keyword?: string
}

export interface CommentItem {
  id: number
  infoId: number
  userId: number
  parentId: number | null
  content: string
  status: number
  createTime: string
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

/** 评论列表 */
export function getCommentList(params: CommentQuery) {
  return request<ApiResponse<PageResult<CommentItem>>>({
    url: "/admin/comment/list",
    method: "get",
    params
  })
}

/** 屏蔽评论 */
export function hideComment(id: number) {
  return request<ApiResponse<null>>({
    url: `/admin/comment/${id}/hide`,
    method: "put"
  })
}

/** 取消屏蔽 */
export function showComment(id: number) {
  return request<ApiResponse<null>>({
    url: `/admin/comment/${id}/show`,
    method: "put"
  })
}

/** 删除评论 */
export function deleteComment(id: number) {
  return request<ApiResponse<null>>({
    url: `/admin/comment/${id}`,
    method: "delete"
  })
}

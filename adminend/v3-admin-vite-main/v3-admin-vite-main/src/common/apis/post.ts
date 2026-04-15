import { request } from "@/http/axios"

export type PostStatus = 0 | 1 | 2 | 3

export interface PendingPostQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  categoryId?: number
}

export interface PostQuery extends PendingPostQuery {
  status?: PostStatus
  userId?: number
  sourceType?: string
}

export interface PostItem {
  id: number
  userId: number
  userNickname: string | null
  userAvatar: string | null
  categoryId: number
  categoryName: string | null
  title: string
  contentPreview: string
  coverImage: string | null
  status: PostStatus
  viewCount: number
  commentCount: number
  likeCount: number
  sourceType: string
  createTime: string
}

export interface PostDetail {
  id: number
  userId: number
  userNickname: string | null
  userAvatar: string | null
  categoryId: number
  categoryName: string | null
  title: string
  content: string
  coverImage: string | null
  imageList: string[]
  status: PostStatus
  viewCount: number
  commentCount: number
  likeCount: number
  sourceType: string
  sourceRecordId: number | null
  auditReason: string | null
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

/** 待审核帖子列表 */
export function getPendingPostList(params: PendingPostQuery) {
  return request<ApiResponse<PageResult<PostItem>>>({
    url: "/admin/post/pending",
    method: "get",
    params
  })
}

/** 全部帖子列表 */
export function getPostList(params: PostQuery) {
  return request<ApiResponse<PageResult<PostItem>>>({
    url: "/admin/post/list",
    method: "get",
    params
  })
}

/** 帖子详情 */
export function getPostDetail(id: number) {
  return request<ApiResponse<PostDetail>>({
    url: `/admin/post/${id}`,
    method: "get"
  })
}

/** 审核通过 */
export function approvePost(id: number) {
  return request<ApiResponse<null>>({
    url: `/admin/post/${id}/approve`,
    method: "put"
  })
}

/** 驳回帖子 */
export function rejectPost(id: number, auditReason: string) {
  return request<ApiResponse<null>>({
    url: `/admin/post/${id}/reject`,
    method: "put",
    data: { auditReason }
  })
}

/** 更新帖子状态 */
export function updatePostStatus(id: number, status: PostStatus) {
  return request<ApiResponse<null>>({
    url: `/admin/post/${id}/status`,
    method: "put",
    data: { status }
  })
}

import { request } from "@/http/axios"

export interface UserQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  status?: number
}

export interface UserItem {
  id: number
  openid: string
  nickname: string
  avatar: string
  realName: string | null
  studentNo: string | null
  phone: string | null
  email: string | null
  gender: number | null
  status: number
  lastLoginTime: string | null
  createTime: string
}

export interface UserDetail extends UserItem {
  updateTime: string | null
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

/** 用户列表 */
export function getUserList(params: UserQuery) {
  return request<ApiResponse<PageResult<UserItem>>>({
    url: "/admin/user/list",
    method: "get",
    params
  })
}

/** 用户详情 */
export function getUserDetail(id: number) {
  return request<ApiResponse<UserDetail>>({
    url: `/admin/user/${id}`,
    method: "get"
  })
}

/** 启用/禁用用户 */
export function updateUserStatus(id: number, status: 0 | 1) {
  return request<ApiResponse<null>>({
    url: `/admin/user/${id}/status`,
    method: "put",
    data: { status }
  })
}

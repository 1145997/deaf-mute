import { request } from "@/http/axios"

export interface GestureLibraryQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  status?: number
  isBuiltin?: number
}

export interface GestureLibraryItem {
  id: number
  gestureCode: string
  gestureName: string
  description: string | null
  previewImage: string | null
  status: number
  sort: number
  isBuiltin: number
  detectionKey: string
  createTime: string
  updateTime: string
}

export interface GestureLibraryFormData {
  gestureCode: string
  gestureName: string
  description: string
  previewImage: string
  status: number
  sort: number
  isBuiltin: number
  detectionKey: string
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

export function getGestureLibraryList(params: GestureLibraryQuery) {
  return request<ApiResponse<PageResult<GestureLibraryItem>>>({
    url: "/admin/gesture-library/list",
    method: "get",
    params
  })
}

export function getGestureLibraryDetail(id: number) {
  return request<ApiResponse<GestureLibraryItem>>({
    url: `/admin/gesture-library/${id}`,
    method: "get"
  })
}

export function createGestureLibrary(data: GestureLibraryFormData) {
  return request<ApiResponse<null>>({
    url: "/admin/gesture-library",
    method: "post",
    data
  })
}

export function updateGestureLibrary(id: number, data: GestureLibraryFormData) {
  return request<ApiResponse<null>>({
    url: `/admin/gesture-library/${id}`,
    method: "put",
    data
  })
}

export function updateGestureLibraryStatus(id: number, status: number) {
  return request<ApiResponse<null>>({
    url: `/admin/gesture-library/${id}/status`,
    method: "put",
    data: { status }
  })
}

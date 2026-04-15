import { request } from "@/http/axios"

export interface RecognitionConfigQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  activeFlag?: number
}

export interface RecognitionConfigItem {
  id: number
  configName: string
  confidenceMin: number
  holdMs: number
  debounceMs: number
  cooldownMs: number
  requiredHits: number
  maxIntervalMs: number
  lockTimeoutMs: number
  resetOnFail: number
  allowRepeat: number
  gestureOrderJson: string | null
  activeFlag: number
  remark: string | null
  createTime: string
  updateTime: string
}

export interface RecognitionConfigFormData {
  configName: string
  confidenceMin: number
  holdMs: number
  debounceMs: number
  cooldownMs: number
  requiredHits: number
  maxIntervalMs: number
  lockTimeoutMs: number
  resetOnFail: number
  allowRepeat: number
  gestureOrderJson: string
  activeFlag: number
  remark: string
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

export function getRecognitionConfigList(params: RecognitionConfigQuery) {
  return request<ApiResponse<PageResult<RecognitionConfigItem>>>({
    url: "/admin/recognition-config/list",
    method: "get",
    params
  })
}

export function getActiveRecognitionConfig() {
  return request<ApiResponse<RecognitionConfigItem>>({
    url: "/admin/recognition-config/active",
    method: "get"
  })
}

export function getRecognitionConfigDetail(id: number) {
  return request<ApiResponse<RecognitionConfigItem>>({
    url: `/admin/recognition-config/${id}`,
    method: "get"
  })
}

export function createRecognitionConfig(data: RecognitionConfigFormData) {
  return request<ApiResponse<null>>({
    url: "/admin/recognition-config",
    method: "post",
    data
  })
}

export function updateRecognitionConfig(id: number, data: RecognitionConfigFormData) {
  return request<ApiResponse<null>>({
    url: `/admin/recognition-config/${id}`,
    method: "put",
    data
  })
}

export function activateRecognitionConfig(id: number) {
  return request<ApiResponse<null>>({
    url: `/admin/recognition-config/${id}/activate`,
    method: "put"
  })
}

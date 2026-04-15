import { request } from "@/http/axios"

export interface PhraseTemplateQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  sceneType?: string
  status?: number
}

export interface PhraseTemplateItem {
  id: number
  phraseCode: string
  phraseText: string
  ttsText: string | null
  sceneType: string | null
  status: number
  sort: number
  createTime: string
  updateTime: string
}

export interface PhraseTemplateFormData {
  phraseCode: string
  phraseText: string
  ttsText: string
  sceneType: string
  status: number
  sort: number
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

export function getPhraseTemplateList(params: PhraseTemplateQuery) {
  return request<ApiResponse<PageResult<PhraseTemplateItem>>>({
    url: "/admin/phrase-template/list",
    method: "get",
    params
  })
}

export function getPhraseTemplateDetail(id: number) {
  return request<ApiResponse<PhraseTemplateItem>>({
    url: `/admin/phrase-template/${id}`,
    method: "get"
  })
}

export function createPhraseTemplate(data: PhraseTemplateFormData) {
  return request<ApiResponse<null>>({
    url: "/admin/phrase-template",
    method: "post",
    data
  })
}

export function updatePhraseTemplate(id: number, data: PhraseTemplateFormData) {
  return request<ApiResponse<null>>({
    url: `/admin/phrase-template/${id}`,
    method: "put",
    data
  })
}

export function updatePhraseTemplateStatus(id: number, status: number) {
  return request<ApiResponse<null>>({
    url: `/admin/phrase-template/${id}/status`,
    method: "put",
    data: { status }
  })
}

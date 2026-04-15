import { request } from "@/http/axios"

export interface GestureFlowQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  flowType?: string
  status?: number
  isBuiltin?: number
}

export interface GestureFlowItem {
  id: number
  flowCode: string
  flowName: string
  flowType: string
  triggerMode: string
  status: number
  priority: number
  versionNo: number
  startNodeId: number | null
  description: string | null
  isBuiltin: number
  createTime: string
  updateTime: string
}

export interface GestureFlowNodeItem {
  id: number
  nodeCode: string
  nodeName: string
  parentNodeId: number | null
  gestureLibraryId: number
  gestureCode: string | null
  gestureName: string | null
  isStart: number
  isEnd: number
  nodeOrder: number
  confidenceMin: number | null
  holdMs: number | null
  debounceMs: number | null
  cooldownMs: number | null
  requiredHits: number | null
  maxIntervalMs: number | null
  resetOnFail: number | null
  allowRepeat: number | null
  successNextStrategy: string | null
  failStrategy: string | null
  remark: string | null
  createTime: string
}

export interface GestureFlowOutputItem {
  id: number
  endNodeId: number | null
  endNodeCode: string | null
  endNodeName: string | null
  outputType: string
  outputText: string | null
  phraseTemplateId: number | null
  phraseText: string | null
  controlAction: string | null
  ttsText: string | null
  displayText: string | null
  createTime: string
}

export interface GestureFlowDetail extends GestureFlowItem {
  nodeList: GestureFlowNodeItem[]
  outputList: GestureFlowOutputItem[]
}

export interface GestureFlowNodeFormData {
  id: number | null
  nodeCode: string
  nodeName: string
  parentNodeId: number | null
  parentNodeCode?: string
  gestureLibraryId: number | null
  isStart: number
  isEnd: number
  nodeOrder: number
  confidenceMin: number | null
  holdMs: number | null
  debounceMs: number | null
  cooldownMs: number | null
  requiredHits: number | null
  maxIntervalMs: number | null
  resetOnFail: number
  allowRepeat: number
  successNextStrategy: string
  failStrategy: string
  remark: string
}

export interface GestureFlowOutputFormData {
  id: number | null
  endNodeId: number | null
  endNodeCode?: string
  outputType: string
  outputText: string
  phraseTemplateId: number | null
  controlAction: string
  ttsText: string
  displayText: string
}

export interface GestureFlowFormData {
  flowCode: string
  flowName: string
  flowType: string
  triggerMode: string
  status: number
  priority: number
  isBuiltin: number
  description: string
  nodeList: GestureFlowNodeFormData[]
  outputList: GestureFlowOutputFormData[]
}

export interface GestureLibraryOptionItem {
  id: number
  gestureCode: string
  gestureName: string
  description: string | null
  previewImage: string | null
  status: number
  sort: number
  isBuiltin: number
  detectionKey: string
}

export interface PhraseTemplateOptionItem {
  id: number
  phraseCode: string
  phraseText: string
  ttsText: string | null
  sceneType: string | null
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

export function getGestureFlowList(params: GestureFlowQuery) {
  return request<ApiResponse<PageResult<GestureFlowItem>>>({
    url: "/admin/gesture-flow/list",
    method: "get",
    params
  })
}

export function getGestureFlowDetail(id: number) {
  return request<ApiResponse<GestureFlowDetail>>({
    url: `/admin/gesture-flow/${id}`,
    method: "get"
  })
}

export function createGestureFlow(data: GestureFlowFormData) {
  return request<ApiResponse<null>>({
    url: "/admin/gesture-flow",
    method: "post",
    data
  })
}

export function updateGestureFlow(id: number, data: GestureFlowFormData) {
  return request<ApiResponse<null>>({
    url: `/admin/gesture-flow/${id}`,
    method: "put",
    data
  })
}

export function deleteGestureFlow(id: number) {
  return request<ApiResponse<null>>({
    url: `/admin/gesture-flow/${id}`,
    method: "delete"
  })
}

export function getEnabledGestureLibraryOptions() {
  return request<ApiResponse<GestureLibraryOptionItem[]>>({
    url: "/gesture-library/enabled",
    method: "get"
  })
}

export function getEnabledPhraseTemplateOptions(sceneType?: string) {
  return request<ApiResponse<PhraseTemplateOptionItem[]>>({
    url: "/phrase-template/list",
    method: "get",
    params: {
      sceneType
    }
  })
}

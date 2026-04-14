import { request } from "@/http/axios"

export interface CategoryQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  status?: number
}

export interface CategoryItem {
  id: number
  name: string
  sort: number
  status: number
  createTime: string
}

export interface CategoryFormData {
  name: string
  sort: number
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

/** 分类列表 */
export function getCategoryList(params: CategoryQuery) {
  return request<ApiResponse<PageResult<CategoryItem>>>({
    url: "/admin/category/list",
    method: "get",
    params
  })
}

/** 新增分类 */
export function createCategory(data: CategoryFormData) {
  return request<ApiResponse<null>>({
    url: "/admin/category",
    method: "post",
    data
  })
}

/** 修改分类 */
export function updateCategory(id: number, data: CategoryFormData) {
  return request<ApiResponse<null>>({
    url: `/admin/category/${id}`,
    method: "put",
    data
  })
}

/** 删除分类 */
export function deleteCategory(id: number) {
  return request<ApiResponse<null>>({
    url: `/admin/category/${id}`,
    method: "delete"
  })
}

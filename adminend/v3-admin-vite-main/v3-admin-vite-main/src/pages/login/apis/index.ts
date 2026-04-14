import { request } from "@/http/axios"

export interface AdminLoginRequestData {
  username: string
  password: string
}

export interface AdminLoginResponseData {
  code: number
  message: string
  data: {
    token: string
    username: string
    nickname: string
    role: string
  }
}

export interface AdminInfoResponseData {
  code: number
  message: string
  data: {
    id: number
    username: string
    nickname: string
    role: string
  }
}

export function loginApi(data: AdminLoginRequestData) {
  return request<AdminLoginResponseData>({
    url: "/auth/admin/login",
    method: "post",
    data
  })
}

export function getAdminInfoApi() {
  return request<AdminInfoResponseData>({
    url: "/auth/admin/info",
    method: "get"
  })
}

export function logoutApi() {
  return request({
    url: "/auth/admin/logout",
    method: "post"
  })
}

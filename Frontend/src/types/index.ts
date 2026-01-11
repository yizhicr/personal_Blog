// 用户相关类型
export interface User {
  id: number
  username: string
  email: string
  nickname: string
  avatar?: string
}

// 登录请求
export interface LoginRequest {
  username: string
  password: string
}

// 注册请求
export interface RegisterRequest {
  username: string
  email: string
  password: string
  nickname: string
}

// 通用响应格式
export interface ApiResponse<T = any> {
  success: boolean
  data: T
  message?: string
}

// 分页参数
export interface PaginationParams {
  page: number
  size: number
}

// 分页响应
export interface PaginationResponse<T = any> {
  data: T[]
  pagination: {
    page: number
    size: number
    total: number
  }
}
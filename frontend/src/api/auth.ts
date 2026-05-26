import request from '@/utils/request'

export interface UserProfile {
  id: number
  username: string
  nickname?: string
  email?: string
  avatar?: string
  role?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams extends LoginParams {
  nickname?: string
  email?: string
}

export interface LoginResult {
  token: string
  user: UserProfile
}

export function login(data: LoginParams) {
  return request.post<unknown, LoginResult>('/api/v1/auth/login', data)
}

export function register(data: RegisterParams) {
  return request.post<unknown, UserProfile>('/api/v1/auth/register', data)
}

export function logout() {
  return request.post<unknown, void>('/api/v1/auth/logout')
}

import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

import { useUserStore } from '@/store/user'

export interface Result<T> {
  code: number
  message: string
  data: T
}

const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000
})

service.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const result = response.data as Result<unknown>
    if (typeof result?.code === 'number' && 'message' in result && 'data' in result) {
      if (result.code !== 200) {
        ElMessage.error(result.message || '请求失败')
        return Promise.reject(new Error(result.message || '请求失败'))
      }
      return result.data
    }
    return response.data
  },
  (error: AxiosError<Result<unknown>>) => {
    const message = error.response?.data?.message || error.message || '网络异常'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service

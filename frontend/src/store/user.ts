import { defineStore } from 'pinia'

import { login, logout, register, type LoginParams, type RegisterParams, type UserProfile } from '@/api/auth'

interface UserState {
  token: string
  profile: UserProfile | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('photo_token') || '',
    profile: localStorage.getItem('photo_user') ? JSON.parse(localStorage.getItem('photo_user') || '{}') : null
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token)
  },
  actions: {
    setSession(token: string, profile: UserProfile) {
      this.token = token
      this.profile = profile
      localStorage.setItem('photo_token', token)
      localStorage.setItem('photo_user', JSON.stringify(profile))
    },
    clearSession() {
      this.token = ''
      this.profile = null
      localStorage.removeItem('photo_token')
      localStorage.removeItem('photo_user')
    },
    async login(params: LoginParams) {
      const result = await login(params)
      this.setSession(result.token, result.user)
    },
    async register(params: RegisterParams) {
      const profile = await register(params)
      this.profile = profile
    },
    async logout() {
      try {
        await logout()
      } finally {
        this.clearSession()
      }
    }
  }
})

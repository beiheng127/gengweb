import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'

const USER_KEY = 'gengweb_user'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)

  const setUser = (userData: User | null) => {
    user.value = userData
    if (userData) {
      localStorage.setItem(USER_KEY, JSON.stringify(userData))
    } else {
      localStorage.removeItem(USER_KEY)
    }
  }

  const loadUserFromStorage = () => {
    const storedUser = localStorage.getItem(USER_KEY)
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch (e) {
        console.error('Failed to parse stored user:', e)
        localStorage.removeItem(USER_KEY)
      }
    }
  }

  return {
    user,
    setUser,
    loadUserFromStorage
  }
})

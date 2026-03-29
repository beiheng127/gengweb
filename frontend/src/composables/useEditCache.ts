import { ref, watch, onMounted, onUnmounted } from 'vue'
import { getStorage, setStorage, removeStorage } from '@/utils/storage'

const SAVE_INTERVAL = 3000

export function useEditCache<T>(cacheKey: string, initialValue: T) {
  const data = ref<T>({ ...initialValue })
  const hasUnsavedChanges = ref(false)
  const isRecovered = ref(false)
  let saveTimer: ReturnType<typeof setInterval> | null = null

  const saveToCache = () => {
    setStorage(cacheKey, {
      data: data.value,
      timestamp: Date.now()
    })
  }

  const clearCache = () => {
    removeStorage(cacheKey)
    hasUnsavedChanges.value = false
    isRecovered.value = false
  }

  const recoverFromCache = () => {
    const cached = getStorage<{ data: T; timestamp: number }>(cacheKey)
    if (cached && cached.data) {
      data.value = { ...cached.data }
      hasUnsavedChanges.value = true
      isRecovered.value = true
      return true
    }
    return false
  }

  watch(data, () => {
    hasUnsavedChanges.value = true
  }, { deep: true })

  const startAutoSave = () => {
    saveTimer = setInterval(() => {
      if (hasUnsavedChanges.value) {
        saveToCache()
      }
    }, SAVE_INTERVAL)
  }

  const stopAutoSave = () => {
    if (saveTimer) {
      clearInterval(saveTimer)
      saveTimer = null
    }
  }

  onMounted(() => {
    startAutoSave()
  })

  onUnmounted(() => {
    stopAutoSave()
  })

  return {
    data,
    hasUnsavedChanges,
    isRecovered,
    saveToCache,
    clearCache,
    recoverFromCache
  }
}

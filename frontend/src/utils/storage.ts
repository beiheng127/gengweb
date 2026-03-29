const STORAGE_PREFIX = 'memepedia_'

export function getStorage<T>(key: string): T | null {
  try {
    const data = localStorage.getItem(STORAGE_PREFIX + key)
    if (data) {
      return JSON.parse(data) as T
    }
  } catch (e) {
    console.error('Failed to get storage:', e)
  }
  return null
}

export function setStorage<T>(key: string, value: T): void {
  try {
    localStorage.setItem(STORAGE_PREFIX + key, JSON.stringify(value))
  } catch (e) {
    console.error('Failed to set storage:', e)
  }
}

export function removeStorage(key: string): void {
  localStorage.removeItem(STORAGE_PREFIX + key)
}

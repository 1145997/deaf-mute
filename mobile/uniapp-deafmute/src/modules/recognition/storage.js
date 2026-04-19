export function readStorage(key, fallback = '') {
  try {
    if (typeof uni !== 'undefined' && typeof uni.getStorageSync === 'function') {
      const value = uni.getStorageSync(key)
      return value || fallback
    }
  } catch (error) {
    console.warn('readStorage failed', error)
  }
  return fallback
}

export function writeStorage(key, value) {
  try {
    if (typeof uni !== 'undefined' && typeof uni.setStorageSync === 'function') {
      if (value) {
        uni.setStorageSync(key, value)
      } else if (typeof uni.removeStorageSync === 'function') {
        uni.removeStorageSync(key)
      }
    }
  } catch (error) {
    console.warn('writeStorage failed', error)
  }
}

function resolveDefaultRecognitionApiBase() {
  if (typeof window !== 'undefined' && !window.plus) {
    return '/api'
  }
  return 'http://127.0.0.1:8080/api'
}

const DEFAULT_RECOGNITION_API_BASE = resolveDefaultRecognitionApiBase()

export const RECOGNITION_CLIENT_TYPES = {
  ANDROID: 'UNI_APP_ANDROID',
}

export const RECOGNITION_SCENES = {
  HOME: 'HOME',
}

export const RECOGNITION_ENGINES = {
  MEDIAPIPE_JS: 'MEDIAPIPE_JS',
}

export const RECOGNITION_OUTPUT_TYPES = {
  NONE: 'NONE',
}

export function normalizeRecognitionApiBase(baseUrl = DEFAULT_RECOGNITION_API_BASE) {
  return String(baseUrl || DEFAULT_RECOGNITION_API_BASE).trim().replace(/\/$/, '')
}

export function buildRecognitionApiUrl(baseUrl, path = '') {
  const normalizedBase = normalizeRecognitionApiBase(baseUrl)
  const normalizedPath = String(path || '').startsWith('/') ? path : `/${path}`
  return `${normalizedBase}${normalizedPath}`
}

export function normalizeAuthToken(token = '') {
  const rawToken = String(token || '').trim()
  if (!rawToken) {
    return ''
  }
  return rawToken.startsWith('Bearer ') ? rawToken : `Bearer ${rawToken}`
}

export { DEFAULT_RECOGNITION_API_BASE }

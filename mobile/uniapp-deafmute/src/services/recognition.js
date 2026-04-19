import { buildRecognitionApiUrl, normalizeAuthToken } from '@/config/recognition'

function unwrapResult(response) {
  const body = response?.data
  if (!body || typeof body !== 'object') {
    throw new Error('识别服务返回格式异常')
  }
  if (response.statusCode < 200 || response.statusCode >= 300) {
    throw new Error(body.message || `识别服务请求失败(${response.statusCode})`)
  }
  if (body.code !== 200) {
    throw new Error(body.message || `识别服务返回错误(${body.code || 'unknown'})`)
  }
  return body.data
}

function buildHeaders(token) {
  const headers = {
    'Content-Type': 'application/json',
  }
  const normalizedToken = normalizeAuthToken(token)
  if (normalizedToken) {
    headers.Authorization = normalizedToken
  }
  return headers
}

function requestRecognition({ baseUrl, path, method = 'GET', data, token }) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: buildRecognitionApiUrl(baseUrl, path),
      method,
      data,
      header: buildHeaders(token),
      success(response) {
        try {
          resolve(unwrapResult(response))
        } catch (error) {
          reject(error)
        }
      },
      fail(error) {
        reject(new Error(error?.errMsg || '识别服务网络请求失败'))
      },
    })
  })
}

export function fetchRecognitionBootstrap({ baseUrl, token }) {
  return requestRecognition({
    baseUrl,
    token,
    method: 'GET',
    path: '/recognition/bootstrap',
  })
}

export function startRecognitionSession({ baseUrl, token, payload }) {
  return requestRecognition({
    baseUrl,
    token,
    method: 'POST',
    path: '/recognition/session/start',
    data: payload,
  })
}

export function predictRecognition({ baseUrl, token, payload }) {
  return requestRecognition({
    baseUrl,
    token,
    method: 'POST',
    path: '/recognition/predict',
    data: payload,
  })
}

export function resetRecognitionRuntime({ baseUrl, token, payload }) {
  return requestRecognition({
    baseUrl,
    token,
    method: 'POST',
    path: '/recognition/session/reset',
    data: payload,
  })
}

export function closeRecognitionRuntime({ baseUrl, token, payload }) {
  return requestRecognition({
    baseUrl,
    token,
    method: 'POST',
    path: '/recognition/session/close',
    data: payload,
  })
}

export function fetchMyRecognitionRecords({ baseUrl, token, pageNum = 1, pageSize = 10 }) {
  return requestRecognition({
    baseUrl,
    token,
    method: 'GET',
    path: '/recognition-record/my',
    data: {
      pageNum,
      pageSize,
    },
  })
}

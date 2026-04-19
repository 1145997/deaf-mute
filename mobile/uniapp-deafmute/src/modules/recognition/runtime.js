import {
  closeRecognitionRuntime,
  fetchMyRecognitionRecords,
  fetchRecognitionBootstrap,
  predictRecognition,
  resetRecognitionRuntime,
  startRecognitionSession,
} from '@/services/recognition'
import {
  DEFAULT_RECOGNITION_API_BASE,
  RECOGNITION_CLIENT_TYPES,
  RECOGNITION_ENGINES,
  RECOGNITION_SCENES,
  normalizeRecognitionApiBase,
} from '@/config/recognition'
import {
  RECOGNITION_INPUT_MODES,
  STORAGE_KEYS,
  normalizeInputMode,
} from '@/modules/recognition/constants'
import { buildPredictRequest, normalizeBridgeInputMessage } from '@/modules/recognition/input'
import { readStorage, writeStorage } from '@/modules/recognition/storage'
import {
  applyRecognitionResult,
  pushRuntimeMessage,
  resetFrontendState,
} from '@/modules/recognition/state'

const predictionQueues = new WeakMap()

function isH5DevRuntime() {
  return typeof window !== 'undefined' && !window.plus
}

function shouldMigrateLegacyApiBase(value) {
  return typeof value === 'string'
    && /https?:\/\/(127\.0\.0\.1|localhost):8080\/api/i.test(value)
}

function shouldMigrateLegacyBridgeOrigin(value) {
  return typeof value === 'string'
    && /https?:\/\/(127\.0\.0\.1|localhost):8000/i.test(value)
}

function updateQueue(store, task) {
  const current = predictionQueues.get(store) || Promise.resolve()
  const next = current.catch(() => null).then(task)
  predictionQueues.set(store, next)
  return next
}

function setBackendError(store, message) {
  store.backendConnected = false
  store.backendError = message || 'Unknown backend error.'
  pushRuntimeMessage(store, store.backendError)
}

function setBridgeError(store, message) {
  store.bridgeConnected = false
  store.bridgeError = message || 'Unknown bridge error.'
  pushRuntimeMessage(store, store.bridgeError)
}

function setBackendConnected(store, connected) {
  store.backendConnected = connected
  if (connected) {
    store.backendError = ''
  }
}

function setBridgeConnected(store, connected) {
  store.bridgeConnected = connected
  if (connected) {
    store.bridgeError = ''
  }
}

export function hydrateLocalConfigRuntime(store) {
  if (store.hasHydratedLocalConfig) {
    return
  }

  const storedBridgeOrigin = readStorage(STORAGE_KEYS.BRIDGE_ORIGIN, store.bridgeOrigin)
  const storedApiBase = readStorage(STORAGE_KEYS.API_BASE, store.recognitionApiBase)

  store.bridgeOrigin = isH5DevRuntime() && shouldMigrateLegacyBridgeOrigin(storedBridgeOrigin)
    ? store.bridgeOrigin
    : (storedBridgeOrigin || store.bridgeOrigin)

  store.recognitionApiBase = normalizeRecognitionApiBase(
    isH5DevRuntime() && shouldMigrateLegacyApiBase(storedApiBase)
      ? DEFAULT_RECOGNITION_API_BASE
      : storedApiBase,
  )

  store.authToken = readStorage(STORAGE_KEYS.AUTH_TOKEN, '')
  store.inputMode = normalizeInputMode(readStorage(STORAGE_KEYS.INPUT_MODE, store.inputMode))
  store.hasHydratedLocalConfig = true
}

export function persistBridgeOrigin(store, origin) {
  store.bridgeOrigin = origin || store.bridgeOrigin
  writeStorage(STORAGE_KEYS.BRIDGE_ORIGIN, store.bridgeOrigin)
}

export function persistRecognitionApiBase(store, baseUrl) {
  store.recognitionApiBase = normalizeRecognitionApiBase(baseUrl)
  writeStorage(STORAGE_KEYS.API_BASE, store.recognitionApiBase)
}

export function persistAuthToken(store, token) {
  store.authToken = String(token || '').trim()
  writeStorage(STORAGE_KEYS.AUTH_TOKEN, store.authToken)
}

export function persistInputMode(store, mode) {
  store.inputMode = normalizeInputMode(mode)
  writeStorage(STORAGE_KEYS.INPUT_MODE, store.inputMode)
  pushRuntimeMessage(store, `Input mode switched to ${store.inputMode}.`)
}

export async function ensureBootstrapRuntime(store, { force = false } = {}) {
  hydrateLocalConfigRuntime(store)
  if (store.bootstrapLoaded && !force) {
    return store.activeConfig
  }

  store.bootstrapLoading = true
  store.bootstrapError = ''

  try {
    const payload = await fetchRecognitionBootstrap({
      baseUrl: store.recognitionApiBase,
      token: store.authToken,
    })
    store.activeConfig = payload?.activeConfig || null
    store.gestureCatalog = payload?.gestureList || []
    store.phraseCatalog = payload?.phraseList || []
    store.flowCatalog = payload?.flowList || []
    store.supportedLandmarkGestureCodes = payload?.supportedLandmarkGestureCodes || []
    store.landmarkPointCount = payload?.landmarkPointCount || 0
    store.supportsHandedness = Boolean(payload?.supportsHandedness)
    store.supportsMirrored = Boolean(payload?.supportsMirrored)
    store.landmarkCoordinateMode = payload?.landmarkCoordinateMode || ''
    store.landmarkOrigin = payload?.landmarkOrigin || ''
    store.landmarkOrder = payload?.landmarkOrder || ''
    store.landmarkHandMode = payload?.landmarkHandMode || ''
    store.landmarkAssumptions = payload?.landmarkAssumptions || ''
    store.bootstrapLoaded = true
    setBackendConnected(store, true)
    pushRuntimeMessage(store, `Bootstrap synced: ${store.activeConfig?.configName || 'active config'}.`)
    return store.activeConfig
  } catch (error) {
    store.bootstrapLoaded = false
    store.bootstrapError = error.message
    setBackendError(store, `bootstrap failed: ${error.message}`)
    throw error
  } finally {
    store.bootstrapLoading = false
  }
}

export async function startRecognitionSessionRuntime(store, { force = false } = {}) {
  hydrateLocalConfigRuntime(store)

  if (store.sessionId && store.sessionStatus === 'active' && !force) {
    return store.sessionId
  }

  if (force && store.sessionId) {
    await closeRecognitionSessionRuntime(store, { silent: true })
  }

  store.sessionStatus = 'starting'
  store.sessionError = ''
  await ensureBootstrapRuntime(store)

  try {
    const payload = await startRecognitionSession({
      baseUrl: store.recognitionApiBase,
      token: store.authToken,
      payload: {
        clientType: RECOGNITION_CLIENT_TYPES.ANDROID,
        sceneCode: RECOGNITION_SCENES.HOME,
        engineType: RECOGNITION_ENGINES.MEDIAPIPE_JS,
        appVersion: '0.1.0',
      },
    })
    store.sessionId = payload?.sessionId || ''
    store.sessionStartedAt = payload?.startedAt || ''
    store.sessionActiveConfigId = payload?.activeConfigId || null
    store.sessionStatus = store.sessionId ? 'active' : 'idle'
    setBackendConnected(store, true)
    if (store.sessionId) {
      pushRuntimeMessage(store, `Recognition session started: ${store.sessionId}`)
    }
    return store.sessionId
  } catch (error) {
    store.sessionStatus = 'error'
    store.sessionError = error.message
    setBackendError(store, `session/start failed: ${error.message}`)
    throw error
  }
}

export async function ensureRecognitionSessionRuntime(store) {
  if (store.sessionId && store.sessionStatus === 'active') {
    return store.sessionId
  }
  return startRecognitionSessionRuntime(store)
}

async function sendRecognitionInput(store, input) {
  const sessionId = await ensureRecognitionSessionRuntime(store)
  const nextFrameNo = store.frameNo + 1
  const requestPayload = buildPredictRequest({
    sessionId,
    nextFrameNo,
    input,
  })

  if (!requestPayload) {
    return null
  }

  store.predictLoading = true
  store.frameNo = nextFrameNo
  store.lastInputKind = input.kind
  store.lastInputAt = new Date().toISOString()

  try {
    const result = await predictRecognition({
      baseUrl: store.recognitionApiBase,
      token: store.authToken,
      payload: requestPayload,
    })

    setBackendConnected(store, true)
    const messages = applyRecognitionResult(store, {
      ...result,
      sourceGesture: input.payload?.gestureCode || result?.gesture,
      sourceHandedness: input.payload?.handedness || '',
      sourceMirrored: input.payload?.mirrored,
      sourceCameraFacing: input.payload?.cameraFacing || '',
    }) || []
    messages.forEach(message => pushRuntimeMessage(store, message))
    return result
  } catch (error) {
    setBackendError(store, `predict failed: ${error.message}`)
    throw error
  } finally {
    store.predictLoading = false
  }
}

export function enqueueRecognitionInputRuntime(store, input) {
  return updateQueue(store, () => sendRecognitionInput(store, input))
}

export async function handleBridgeMessageRuntime(store, message) {
  const normalized = normalizeBridgeInputMessage(message, store.inputMode)
  if (!normalized) {
    return null
  }

  if (normalized.kind === 'status') {
    setBridgeConnected(store, normalized.payload?.connected ?? true)
    pushRuntimeMessage(store, normalized.payload?.message || 'Bridge status received.')
    return null
  }

  if (normalized.kind === 'error') {
    setBridgeError(store, normalized.payload?.message || 'Bridge error received.')
    return null
  }

  return enqueueRecognitionInputRuntime(store, normalized)
}

export async function resetRecognitionSessionRuntime(store) {
  if (!store.sessionId) {
    resetFrontendState(store)
    return null
  }

  try {
    await resetRecognitionRuntime({
      baseUrl: store.recognitionApiBase,
      token: store.authToken,
      payload: {
        sessionId: store.sessionId,
        reason: 'android-manual-reset',
      },
    })
    resetFrontendState(store)
    store.sessionStatus = 'active'
    setBackendConnected(store, true)
    pushRuntimeMessage(store, 'Recognition session reset.')
  } catch (error) {
    setBackendError(store, `session/reset failed: ${error.message}`)
    throw error
  }
}

export async function closeRecognitionSessionRuntime(store, { silent = false } = {}) {
  if (!store.sessionId) {
    store.sessionStatus = 'closed'
    resetFrontendState(store)
    return null
  }

  store.sessionStatus = 'closing'
  try {
    await closeRecognitionRuntime({
      baseUrl: store.recognitionApiBase,
      token: store.authToken,
      payload: {
        sessionId: store.sessionId,
        reason: 'android-manual-close',
      },
    })
    store.sessionId = ''
    store.sessionStartedAt = ''
    store.sessionActiveConfigId = null
    store.sessionStatus = 'closed'
    store.sessionError = ''
    resetFrontendState(store)
    setBackendConnected(store, true)
    if (!silent) {
      pushRuntimeMessage(store, 'Recognition session closed.')
    }
  } catch (error) {
    store.sessionStatus = 'error'
    store.sessionError = error.message
    setBackendError(store, `session/close failed: ${error.message}`)
    throw error
  }
}

export async function loadMyRecordsRuntime(store, { pageNum = 1, pageSize = store.recordPageSize, silent = false } = {}) {
  if (!store.authToken) {
    store.recordsError = 'Auth token is required before calling recognition-record/my.'
    store.recordList = []
    store.recordTotal = 0
    if (!silent) {
      pushRuntimeMessage(store, store.recordsError)
    }
    return null
  }

  store.recordsLoading = true
  store.recordsError = ''

  try {
    const payload = await fetchMyRecognitionRecords({
      baseUrl: store.recognitionApiBase,
      token: store.authToken,
      pageNum,
      pageSize,
    })
    store.recordList = payload?.list || []
    store.recordTotal = payload?.total || 0
    store.recordPageNum = payload?.pageNum || pageNum
    store.recordPageSize = payload?.pageSize || pageSize
    setBackendConnected(store, true)
    if (!silent) {
      pushRuntimeMessage(store, `Recognition records refreshed: ${store.recordList.length} item(s).`)
    }
    return store.recordList
  } catch (error) {
    store.recordsError = error.message
    setBackendError(store, `recognition-record/my failed: ${error.message}`)
    throw error
  } finally {
    store.recordsLoading = false
  }
}

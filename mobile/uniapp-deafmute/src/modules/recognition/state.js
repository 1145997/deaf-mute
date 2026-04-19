import { DEFAULT_BRIDGE_ORIGIN } from '@/config/bridge'
import { DEFAULT_RECOGNITION_API_BASE, RECOGNITION_OUTPUT_TYPES } from '@/config/recognition'
import { CAPTURE_MIGRATION_STAGES, CAPTURE_PROVIDERS } from '@/modules/capture/constants'
import {
  CONTROL_ACTION_CLEAR,
  CONTROL_ACTION_DELETE,
  CONTROL_ACTION_RESET,
  DEFAULT_LOCATION_NAME,
  DEFAULT_RESULT_LABEL,
  DEFAULT_STATUS_GESTURE,
  RECOGNITION_INPUT_MODES,
} from '@/modules/recognition/constants'

function normalizeControlAction(action) {
  return String(action || '').trim().toUpperCase()
}

function shouldAppendPrediction(payload) {
  if (!payload || payload.locked) {
    return false
  }
  if (payload.outputType && payload.outputType !== RECOGNITION_OUTPUT_TYPES.NONE) {
    return true
  }
  if (!payload.outputType && payload.code && payload.label) {
    return true
  }
  return false
}

function buildOutputSignature(payload) {
  return [
    payload?.traceId,
    payload?.matchedFlowCode,
    payload?.matchedNodeCode,
    payload?.code,
    payload?.displayText || payload?.label,
    payload?.controlAction,
  ].filter(Boolean).join('|')
}

function formatClockTime() {
  return new Date().toLocaleTimeString('zh-CN', { hour12: false })
}

export function createRecognitionState() {
  return {
    hasHydratedLocalConfig: false,
    locationName: DEFAULT_LOCATION_NAME,
    bridgeOrigin: DEFAULT_BRIDGE_ORIGIN,
    recognitionApiBase: DEFAULT_RECOGNITION_API_BASE,
    authToken: '',
    inputMode: RECOGNITION_INPUT_MODES.LANDMARKS,
    bridgeMode: 'mock',
    captureProvider: CAPTURE_PROVIDERS.WEBVIEW_BRIDGE,
    captureMigrationStage: CAPTURE_MIGRATION_STAGES.BRIDGE_LIVE,
    bridgeConnected: false,
    backendConnected: false,
    bridgeError: '',
    backendError: '',
    bootstrapLoading: false,
    bootstrapLoaded: false,
    bootstrapError: '',
    activeConfig: null,
    gestureCatalog: [],
    phraseCatalog: [],
    flowCatalog: [],
    supportedLandmarkGestureCodes: [],
    landmarkPointCount: 0,
    supportsHandedness: false,
    supportsMirrored: false,
    landmarkCoordinateMode: '',
    landmarkOrigin: '',
    landmarkOrder: '',
    landmarkHandMode: '',
    landmarkAssumptions: '',
    sessionId: '',
    sessionStatus: 'idle',
    sessionStartedAt: '',
    sessionActiveConfigId: null,
    sessionError: '',
    currentGesture: DEFAULT_STATUS_GESTURE,
    currentCode: '',
    currentLabel: DEFAULT_RESULT_LABEL,
    currentDisplayText: '',
    currentOutputType: RECOGNITION_OUTPUT_TYPES.NONE,
    currentControlAction: '',
    currentTraceId: '',
    currentFlowCode: '',
    currentNodeCode: '',
    currentInputType: '',
    currentStableCount: 0,
    currentRequiredHits: 0,
    currentDebounced: false,
    currentDetectedGestureCandidates: [],
    stateText: 'idle',
    locked: false,
    sentenceBuffer: [],
    publishDraft: '',
    lastOutputSignature: '',
    lastUpdatedAt: '',
    predictLoading: false,
    frameNo: 0,
    lastInputKind: '',
    lastInputAt: '',
    lastHandedness: '',
    lastMirrored: null,
    lastCameraFacing: '',
    recordsLoading: false,
    recordsError: '',
    recordList: [],
    recordTotal: 0,
    recordPageNum: 1,
    recordPageSize: 5,
    recentMessages: [
      'Recognition runtime initialized.',
      'Default input mode switched to landmarks.',
    ],
  }
}

export function pushRuntimeMessage(state, message) {
  if (!message) {
    return
  }
  state.recentMessages.unshift(message)
  state.recentMessages = state.recentMessages.slice(0, 8)
}

export function syncSentenceToDraft(state) {
  state.publishDraft = state.sentenceBuffer.join('')
}

export function deleteLastToken(state) {
  if (state.sentenceBuffer.length > 0) {
    state.sentenceBuffer.pop()
  }
  if (state.sentenceBuffer.length === 0) {
    state.lastOutputSignature = ''
  }
  syncSentenceToDraft(state)
}

export function clearSentence(state, { silent = false } = {}) {
  state.sentenceBuffer = []
  state.lastOutputSignature = ''
  syncSentenceToDraft(state)
  if (!silent) {
    pushRuntimeMessage(state, 'Sentence buffer cleared.')
  }
}

export function resetFrontendState(state) {
  state.bridgeError = ''
  state.currentGesture = DEFAULT_STATUS_GESTURE
  state.currentCode = ''
  state.currentLabel = DEFAULT_RESULT_LABEL
  state.currentDisplayText = ''
  state.currentOutputType = RECOGNITION_OUTPUT_TYPES.NONE
  state.currentControlAction = ''
  state.currentTraceId = ''
  state.currentFlowCode = ''
  state.currentNodeCode = ''
  state.currentInputType = ''
  state.currentStableCount = 0
  state.currentRequiredHits = 0
  state.currentDebounced = false
  state.currentDetectedGestureCandidates = []
  state.stateText = 'idle'
  state.locked = false
  state.sentenceBuffer = []
  state.lastOutputSignature = ''
  state.lastUpdatedAt = ''
  state.frameNo = 0
  state.lastInputKind = ''
  state.lastInputAt = ''
  state.lastHandedness = ''
  state.lastMirrored = null
  state.lastCameraFacing = ''
  syncSentenceToDraft(state)
}

function applyControlAction(state, controlAction) {
  const normalized = normalizeControlAction(controlAction)
  if (!normalized) {
    return { handled: false, messages: [] }
  }

  if (CONTROL_ACTION_DELETE.has(normalized)) {
    deleteLastToken(state)
    return { handled: true, messages: [`Control action received: ${normalized}`] }
  }

  if (CONTROL_ACTION_CLEAR.has(normalized)) {
    clearSentence(state, { silent: true })
    return { handled: true, messages: [`Control action received: ${normalized}`] }
  }

  if (CONTROL_ACTION_RESET.has(normalized)) {
    resetFrontendState(state)
    return { handled: true, messages: [`Control action received: ${normalized}`] }
  }

  return { handled: false, messages: [`Control action received: ${normalized}`] }
}

export function applyRecognitionResult(state, payload) {
  if (!payload) {
    return
  }

  const messages = []
  const previousNodeCode = state.currentNodeCode

  state.bridgeConnected = true
  state.backendConnected = true
  state.bridgeError = ''
  state.backendError = ''
  state.stateText = payload.state ?? state.stateText
  state.locked = Boolean(payload.locked ?? state.locked)
  state.currentGesture = payload.gesture || payload.sourceGesture || state.currentGesture
  state.currentCode = payload.code || ''
  state.currentDisplayText = payload.displayText || payload.label || state.currentDisplayText
  state.currentLabel = payload.displayText || payload.label || state.currentLabel
  state.currentOutputType = payload.outputType || RECOGNITION_OUTPUT_TYPES.NONE
  state.currentControlAction = payload.controlAction || ''
  state.currentTraceId = payload.traceId || ''
  state.currentFlowCode = payload.matchedFlowCode || ''
  state.currentNodeCode = payload.matchedNodeCode || ''
  state.currentInputType = payload.inputType || state.currentInputType
  state.currentStableCount = Number.isFinite(Number(payload.stableCount)) ? Number(payload.stableCount) : 0
  state.currentRequiredHits = Number.isFinite(Number(payload.requiredHits)) ? Number(payload.requiredHits) : 0
  state.currentDebounced = Boolean(payload.debounced)
  state.currentDetectedGestureCandidates = Array.isArray(payload.detectedGestureCandidates)
    ? payload.detectedGestureCandidates
    : []
  state.lastHandedness = payload.sourceHandedness || state.lastHandedness
  state.lastMirrored = payload.sourceMirrored ?? state.lastMirrored
  state.lastCameraFacing = payload.sourceCameraFacing || state.lastCameraFacing
  state.lastUpdatedAt = formatClockTime()

  const control = applyControlAction(state, payload.controlAction)
  messages.push(...control.messages)
  if (control.handled) {
    return messages
  }

  if (payload.locked && payload.matchedNodeCode && payload.matchedNodeCode !== previousNodeCode) {
    messages.push(`Flow locked on node: ${payload.matchedNodeCode}`)
  }

  if (!shouldAppendPrediction(payload)) {
    return messages
  }

  const nextToken = payload.displayText || payload.label
  if (!nextToken) {
    return messages
  }

  const outputSignature = buildOutputSignature(payload)
  if (outputSignature && outputSignature === state.lastOutputSignature) {
    return messages
  }

  state.lastOutputSignature = outputSignature
  state.sentenceBuffer.push(nextToken)
  if (state.sentenceBuffer.length > 8) {
    state.sentenceBuffer = state.sentenceBuffer.slice(-8)
  }
  syncSentenceToDraft(state)
  messages.push(
    payload.matchedFlowCode
      ? `Flow output: ${payload.matchedFlowCode} -> ${nextToken}`
      : `Recognition output: ${nextToken}`,
  )

  return messages
}

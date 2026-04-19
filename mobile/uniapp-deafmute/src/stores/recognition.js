import { defineStore } from 'pinia'
import { DEFAULT_BRIDGE_ORIGIN } from '@/config/bridge'
import {
  CAPTURE_MIGRATION_LABELS,
  CAPTURE_PROVIDER_LABELS,
} from '@/modules/capture/constants'
import { RECOGNITION_INPUT_MODES } from '@/modules/recognition/constants'
import {
  closeRecognitionSessionRuntime,
  ensureBootstrapRuntime,
  ensureRecognitionSessionRuntime,
  handleBridgeMessageRuntime,
  hydrateLocalConfigRuntime,
  loadMyRecordsRuntime,
  persistAuthToken,
  persistBridgeOrigin,
  persistInputMode,
  persistRecognitionApiBase,
  resetRecognitionSessionRuntime,
  startRecognitionSessionRuntime,
} from '@/modules/recognition/runtime'
import {
  applyRecognitionResult,
  clearSentence as clearSentenceState,
  createRecognitionState,
  deleteLastToken as deleteLastTokenState,
  pushRuntimeMessage,
  resetFrontendState,
  syncSentenceToDraft,
} from '@/modules/recognition/state'

export const useRecognitionStore = defineStore('recognition', {
  state: () => createRecognitionState(),
  getters: {
    sentenceText: state => state.sentenceBuffer.join(''),
    displayText: state => state.sentenceBuffer.join('') || state.currentDisplayText || state.currentLabel,
    bridgeStatusText: state => {
      if (state.bridgeError) {
        return `Bridge error: ${state.bridgeError}`
      }
      if (state.bridgeConnected) {
        return 'Bridge connected'
      }
      if (state.bridgeMode === 'mock') {
        return 'Mock bridge mode'
      }
      return 'Bridge not connected'
    },
    backendStatusText: state => {
      if (state.backendError) {
        return `Backend error: ${state.backendError}`
      }
      if (state.bootstrapLoading || state.predictLoading) {
        return 'Backend syncing'
      }
      if (state.backendConnected) {
        return 'Backend connected'
      }
      return 'Backend not connected'
    },
    sessionStatusText: state => {
      if (state.sessionError) {
        return `Session error: ${state.sessionError}`
      }
      if (state.sessionStatus === 'active' && state.sessionId) {
        return `Session active: ${state.sessionId}`
      }
      if (state.sessionStatus === 'starting') {
        return 'Session starting'
      }
      if (state.sessionStatus === 'closing') {
        return 'Session closing'
      }
      if (state.sessionStatus === 'closed') {
        return 'Session closed'
      }
      return 'Session idle'
    },
    configSummaryText: state => {
      const requiredHits = state.activeConfig?.requiredHits ?? '-'
      const debounceMs = state.activeConfig?.debounceMs ?? '-'
      const maxIntervalMs = state.activeConfig?.maxIntervalMs ?? '-'
      return `requiredHits ${requiredHits} / debounceMs ${debounceMs} / maxIntervalMs ${maxIntervalMs}ms`
    },
    authTokenConfigured: state => Boolean(state.authToken),
    inputModeLabel: state => (
      state.inputMode === RECOGNITION_INPUT_MODES.GESTURE_CODE
        ? 'gestureCode bridge'
        : 'landmarks direct'
    ),
    captureProviderLabel: state => CAPTURE_PROVIDER_LABELS[state.captureProvider] || state.captureProvider,
    captureMigrationLabel: state => CAPTURE_MIGRATION_LABELS[state.captureMigrationStage] || state.captureMigrationStage,
    landmarkSupportText: state => (
      state.supportedLandmarkGestureCodes.length
        ? state.supportedLandmarkGestureCodes.join(', ')
        : 'No landmark capabilities loaded'
    ),
    landmarkContractText: state => {
      if (!state.landmarkCoordinateMode) {
        return 'Landmark contract not loaded'
      }
      return [
        state.landmarkPointCount ? `${state.landmarkPointCount}pt` : '',
        state.landmarkCoordinateMode,
        state.landmarkOrigin,
        state.landmarkOrder,
        state.landmarkHandMode,
      ].filter(Boolean).join(' / ')
    },
    predictDebugText: state => {
      const stable = state.currentRequiredHits
        ? `${state.currentStableCount}/${state.currentRequiredHits}`
        : String(state.currentStableCount || 0)
      const candidates = state.currentDetectedGestureCandidates.length
        ? state.currentDetectedGestureCandidates.join(', ')
        : '-'
      return `input ${state.currentInputType || '-'} / stable ${stable} / debounced ${state.currentDebounced ? 'yes' : 'no'} / candidates ${candidates}`
    },
    inputMetaText: state => {
      const mirrored = state.lastMirrored == null ? '-' : (state.lastMirrored ? 'true' : 'false')
      return `handedness ${state.lastHandedness || '-'} / mirrored ${mirrored} / camera ${state.lastCameraFacing || '-'}`
    },
  },
  actions: {
    hydrateLocalConfig() {
      hydrateLocalConfigRuntime(this)
    },
    setBridgeMode(mode) {
      this.bridgeMode = mode
    },
    setCaptureProvider(provider) {
      this.captureProvider = provider
    },
    setCaptureMigrationStage(stage) {
      this.captureMigrationStage = stage
    },
    setBridgeOrigin(origin) {
      persistBridgeOrigin(this, origin || DEFAULT_BRIDGE_ORIGIN)
    },
    setRecognitionApiBase(baseUrl) {
      persistRecognitionApiBase(this, baseUrl)
    },
    setAuthToken(token) {
      persistAuthToken(this, token)
    },
    setInputMode(mode) {
      persistInputMode(this, mode)
    },
    async ensureBootstrap(options) {
      return ensureBootstrapRuntime(this, options)
    },
    async startRecognitionSession(options) {
      return startRecognitionSessionRuntime(this, options)
    },
    async ensureRecognitionSession() {
      return ensureRecognitionSessionRuntime(this)
    },
    async resetRecognitionSession() {
      return resetRecognitionSessionRuntime(this)
    },
    async closeRecognitionSession(options) {
      return closeRecognitionSessionRuntime(this, options)
    },
    async loadMyRecords(options) {
      return loadMyRecordsRuntime(this, options)
    },
    async applyBridgeMessage(message) {
      return handleBridgeMessageRuntime(this, message)
    },
    seedMockRecognition() {
      const messages = applyRecognitionResult(this, {
        gesture: 'is_v_sign',
        code: 'single_hello',
        label: '你好',
        displayText: '你好',
        outputType: 'TEXT',
        locked: false,
        state: 'idle',
        traceId: `mock_${Date.now()}`,
        matchedFlowCode: 'mock_single',
        matchedNodeCode: 'node_mock_v',
      }) || []

      messages.forEach(message => pushRuntimeMessage(this, message))
    },
    deleteLastToken() {
      deleteLastTokenState(this)
    },
    clearSentence(options) {
      clearSentenceState(this, options)
    },
    syncSentenceToDraft() {
      syncSentenceToDraft(this)
    },
    setPublishDraft(value) {
      this.publishDraft = value
    },
    resetSession() {
      resetFrontendState(this)
      pushRuntimeMessage(this, 'Frontend recognition state reset.')
    },
    pushMessage(message) {
      pushRuntimeMessage(this, message)
    },
  },
})

function resolveDefaultBridgeOrigin() {
  if (typeof window !== 'undefined' && !window.plus) {
    return '/blind'
  }
  return 'http://127.0.0.1:8000'
}

const DEFAULT_BRIDGE_ORIGIN = resolveDefaultBridgeOrigin()
const DEFAULT_BRIDGE_FRAME_INTERVAL_MS = 120
const DEFAULT_BRIDGE_CAMERA_FACING = 'front'
const DEFAULT_BRIDGE_MIRRORED = false
const DEFAULT_BRIDGE_LANDMARK_SOURCE = 'android-landmarks'

export const BRIDGE_INPUT_MODES = {
  GESTURE_CODE: 'gestureCode',
  LANDMARKS: 'landmarks',
}

export const BRIDGE_MESSAGE_TYPES = {
  STATUS: 'bridge-status',
  PREDICT: 'gesture-predict',
  LANDMARKS: 'gesture-landmarks',
  ERROR: 'bridge-error',
}

function normalizeBridgeInputMode(mode) {
  return mode === BRIDGE_INPUT_MODES.GESTURE_CODE
    ? BRIDGE_INPUT_MODES.GESTURE_CODE
    : BRIDGE_INPUT_MODES.LANDMARKS
}

function buildBridgeQuery(inputMode, options = {}) {
  const { layout = 'full' } = options
  return new URLSearchParams({
    bridgeTarget: 'uniapp',
    bridgeMode: 'postMessage',
    bridgeInputMode: normalizeBridgeInputMode(inputMode),
    bridgeFrameIntervalMs: String(DEFAULT_BRIDGE_FRAME_INTERVAL_MS),
    bridgeCameraFacing: DEFAULT_BRIDGE_CAMERA_FACING,
    bridgeMirrored: String(DEFAULT_BRIDGE_MIRRORED),
    bridgeLandmarkSource: DEFAULT_BRIDGE_LANDMARK_SOURCE,
    bridgeLayout: layout,
  })
}

export function buildBlindBridgeUrl(origin = DEFAULT_BRIDGE_ORIGIN, inputMode = BRIDGE_INPUT_MODES.GESTURE_CODE, options = {}) {
  const normalizedOrigin = String(origin || DEFAULT_BRIDGE_ORIGIN).replace(/\/$/, '')
  const params = buildBridgeQuery(inputMode, options)
  return `${normalizedOrigin}/?${params.toString()}`
}

export function buildBlindPreviewUrl(origin = DEFAULT_BRIDGE_ORIGIN, inputMode = BRIDGE_INPUT_MODES.LANDMARKS) {
  return buildBlindBridgeUrl(origin, inputMode, { layout: 'preview' })
}

export function normalizeBridgeMessages(event) {
  const payload = event?.detail?.data ?? event
  if (Array.isArray(payload)) {
    return payload
  }
  if (payload == null) {
    return []
  }
  return [payload]
}

export {
  DEFAULT_BRIDGE_CAMERA_FACING,
  DEFAULT_BRIDGE_FRAME_INTERVAL_MS,
  DEFAULT_BRIDGE_LANDMARK_SOURCE,
  DEFAULT_BRIDGE_MIRRORED,
  DEFAULT_BRIDGE_ORIGIN,
}

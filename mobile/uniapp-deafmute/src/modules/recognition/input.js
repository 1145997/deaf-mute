import { BRIDGE_MESSAGE_TYPES } from '@/config/bridge'
import { RECOGNITION_INPUT_MODES } from '@/modules/recognition/constants'

function isFiniteNumber(value) {
  return typeof value === 'number' && Number.isFinite(value)
}

export function normalizeGestureCode(payload) {
  const candidates = [
    payload?.gestureCode,
    payload?.gesture,
    payload?.detectionKey,
    payload?.code,
  ]
  return candidates.find(item => typeof item === 'string' && (item.startsWith('is_') || item === 'fist')) || ''
}

export function normalizeLandmarksPayload(payload) {
  if (!Array.isArray(payload?.landmarks)) {
    return null
  }

  const landmarks = payload.landmarks
    .map(point => ({
      x: Number(point?.x),
      y: Number(point?.y),
      z: isFiniteNumber(Number(point?.z)) ? Number(point?.z) : 0,
    }))
    .filter(point => Number.isFinite(point.x) && Number.isFinite(point.y) && Number.isFinite(point.z))

  if (landmarks.length !== 21) {
    return null
  }

  return {
    landmarks,
    frameNo: Number.isFinite(Number(payload?.frameNo)) ? Number(payload.frameNo) : null,
    capturedAt: Number.isFinite(Number(payload?.capturedAt)) ? Number(payload.capturedAt) : Date.now(),
    source: typeof payload?.source === 'string' && payload.source.trim()
      ? payload.source.trim()
      : 'android-landmarks',
    handedness: payload?.handedness || null,
    mirrored: Boolean(payload?.mirrored),
    cameraFacing: typeof payload?.cameraFacing === 'string' && payload.cameraFacing.trim()
      ? payload.cameraFacing.trim()
      : 'front',
  }
}

export function normalizeBridgeInputMessage(message, preferredMode = RECOGNITION_INPUT_MODES.LANDMARKS) {
  if (!message) {
    return null
  }

  const { type, payload = message } = message

  if (type === BRIDGE_MESSAGE_TYPES.STATUS) {
    return { kind: 'status', payload }
  }
  if (type === BRIDGE_MESSAGE_TYPES.ERROR) {
    return { kind: 'error', payload }
  }

  const landmarkPayload = normalizeLandmarksPayload(payload)
  if (type === BRIDGE_MESSAGE_TYPES.LANDMARKS || (preferredMode === RECOGNITION_INPUT_MODES.LANDMARKS && landmarkPayload)) {
    if (!landmarkPayload) {
      return null
    }
    return { kind: RECOGNITION_INPUT_MODES.LANDMARKS, payload: landmarkPayload }
  }

  const gestureCode = normalizeGestureCode(payload)
  if (type === BRIDGE_MESSAGE_TYPES.PREDICT || gestureCode) {
    if (!gestureCode) {
      return null
    }
    return {
      kind: RECOGNITION_INPUT_MODES.GESTURE_CODE,
      payload: {
        gestureCode,
        source: typeof payload?.source === 'string' && payload.source.trim()
          ? payload.source.trim()
          : 'android-bridge',
      },
    }
  }

  return null
}

export function buildPredictRequest({ sessionId, nextFrameNo, input }) {
  if (!sessionId || !input) {
    return null
  }

  if (input.kind === RECOGNITION_INPUT_MODES.LANDMARKS) {
    return {
      sessionId,
      frameNo: input.payload.frameNo ?? nextFrameNo,
      capturedAt: input.payload.capturedAt ?? Date.now(),
      source: input.payload.source || 'android-landmarks',
      handedness: input.payload.handedness || null,
      mirrored: Boolean(input.payload.mirrored),
      cameraFacing: input.payload.cameraFacing || 'front',
      landmarks: input.payload.landmarks,
    }
  }

  if (input.kind === RECOGNITION_INPUT_MODES.GESTURE_CODE) {
    return {
      sessionId,
      frameNo: nextFrameNo,
      capturedAt: Date.now(),
      source: input.payload.source || 'android-bridge',
      gestureCode: input.payload.gestureCode,
    }
  }

  return null
}

export const RECOGNITION_INPUT_MODES = {
  GESTURE_CODE: 'gestureCode',
  LANDMARKS: 'landmarks',
}

export const STORAGE_KEYS = {
  BRIDGE_ORIGIN: 'recognition_bridge_origin',
  API_BASE: 'recognition_api_base',
  AUTH_TOKEN: 'recognition_auth_token',
  INPUT_MODE: 'recognition_input_mode',
}

export const CONTROL_ACTION_DELETE = new Set(['BACKSPACE', 'DELETE', 'DELETE_LAST', 'DELETE_LAST_TOKEN'])
export const CONTROL_ACTION_CLEAR = new Set(['CLEAR', 'CLEAR_RESULT', 'CLEAR_SENTENCE', 'CLEAR_TEXT'])
export const CONTROL_ACTION_RESET = new Set(['RESET', 'RESET_RECOGNITION', 'RESET_SESSION'])

export const DEFAULT_LOCATION_NAME = 'Shanghai'
export const DEFAULT_STATUS_GESTURE = 'Waiting for input'
export const DEFAULT_RESULT_LABEL = 'Recognition result will appear here.'
export const DEFAULT_BRIDGE_FRAME_INTERVAL_MS = 120

export function normalizeInputMode(mode) {
  return mode === RECOGNITION_INPUT_MODES.GESTURE_CODE
    ? RECOGNITION_INPUT_MODES.GESTURE_CODE
    : RECOGNITION_INPUT_MODES.LANDMARKS
}

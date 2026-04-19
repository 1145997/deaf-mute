import { DEV_HOST_TARGETS, LAN_TEMPLATE_HOST } from '@/modules/capture/constants'

function safeParseUrl(url) {
  try {
    return new URL(String(url || '').trim())
  } catch (error) {
    return null
  }
}

export function replaceUrlHost(url, nextHost) {
  const parsed = safeParseUrl(url)
  if (!parsed || !nextHost) {
    return String(url || '')
  }
  parsed.hostname = nextHost
  return parsed.toString().replace(/\/$/, '')
}

export function buildEndpointPresets({ bridgeOrigin, recognitionApiBase }) {
  return {
    [DEV_HOST_TARGETS.LOCALHOST]: {
      key: DEV_HOST_TARGETS.LOCALHOST,
      label: 'Use localhost',
      bridgeOrigin: replaceUrlHost(bridgeOrigin, '127.0.0.1'),
      recognitionApiBase: replaceUrlHost(recognitionApiBase, '127.0.0.1'),
      hint: 'Desktop browser or same host runtime.',
    },
    [DEV_HOST_TARGETS.EMULATOR]: {
      key: DEV_HOST_TARGETS.EMULATOR,
      label: 'Use emulator',
      bridgeOrigin: replaceUrlHost(bridgeOrigin, '10.0.2.2'),
      recognitionApiBase: replaceUrlHost(recognitionApiBase, '10.0.2.2'),
      hint: 'Android emulator loopback mapping.',
    },
    [DEV_HOST_TARGETS.LAN_TEMPLATE]: {
      key: DEV_HOST_TARGETS.LAN_TEMPLATE,
      label: 'Use LAN template',
      bridgeOrigin: replaceUrlHost(bridgeOrigin, LAN_TEMPLATE_HOST),
      recognitionApiBase: replaceUrlHost(recognitionApiBase, LAN_TEMPLATE_HOST),
      hint: 'Replace with your workstation LAN IP for real devices.',
    },
  }
}

function normalizeCameraPermission(value) {
  if (value == null || value === '') {
    return 'unknown'
  }
  if (value === true || value === 'authorized') {
    return 'authorized'
  }
  if (value === false || value === 'denied') {
    return 'denied'
  }
  if (typeof value === 'string') {
    return value
  }
  return 'unknown'
}

export function describeCameraPermission(status) {
  switch (status) {
    case 'authorized':
      return 'authorized'
    case 'denied':
      return 'denied'
    case 'not determined':
      return 'not determined'
    case 'config error':
      return 'manifest/config error'
    default:
      return 'unknown'
  }
}

export function collectRuntimePreflight() {
  const snapshot = {
    platform: 'unknown',
    deviceType: 'unknown',
    osName: 'unknown',
    osVersion: 'unknown',
    uniPlatform: 'unknown',
    cameraPermission: 'unknown',
    hostHint: 'Use emulator host mapping or LAN IP for device testing.',
  }

  try {
    if (typeof uni !== 'undefined' && typeof uni.getSystemInfoSync === 'function') {
      const systemInfo = uni.getSystemInfoSync()
      snapshot.platform = systemInfo.platform || 'unknown'
      snapshot.deviceType = systemInfo.deviceType || 'unknown'
      snapshot.osName = systemInfo.osName || 'unknown'
      snapshot.osVersion = systemInfo.osVersion || 'unknown'
      snapshot.uniPlatform = systemInfo.uniPlatform || 'unknown'
    }
  } catch (error) {
    // Keep fallbacks for desktop build environments.
  }

  try {
    if (typeof uni !== 'undefined' && typeof uni.getAppAuthorizeSetting === 'function') {
      const settings = uni.getAppAuthorizeSetting()
      snapshot.cameraPermission = normalizeCameraPermission(settings?.cameraAuthorized)
    }
  } catch (error) {
    snapshot.cameraPermission = 'unknown'
  }

  if (snapshot.platform === 'android') {
    snapshot.hostHint = 'Emulator usually needs 10.0.2.2, real device needs workstation LAN IP.'
  } else if (snapshot.platform === 'ios') {
    snapshot.hostHint = 'Use workstation LAN IP on physical iOS devices.'
  }

  return snapshot
}

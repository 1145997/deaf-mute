package dev.forint.deafmute.cameraxdemo.domain

object RecognitionConstants {
    const val APP_VERSION = "0.1.0"
    const val CLIENT_TYPE = "android"
    const val SCENE_CODE = "bridge"
    const val ENGINE_TYPE = "mediapipe"
    const val SOURCE = "android-landmarks"

    const val PREDICT_INTERVAL_MS = 350L
    const val COUNTDOWN_TIMEOUT_MS = 3000L
    const val COUNTDOWN_TICK_MS = 250L
    const val MAX_BUFFER_TOKENS = 12

    const val CLOSE_REASON_PAGE_CLOSE = "page-close"
    const val CLOSE_REASON_MANUAL_CLOSE = "manual-close"
    const val CLOSE_REASON_RESTART = "restart-session"

    const val MODEL_ASSET_NAME = "hand_landmarker.task"
}

internal val deleteControlActions = setOf(
    "BACKSPACE",
    "DELETE",
    "DELETE_LAST",
    "DELETE_LAST_TOKEN",
)

internal val clearControlActions = setOf(
    "CLEAR",
    "CLEAR_ALL",
    "CLEAR_RESULT",
    "CLEAR_SENTENCE",
    "CLEAR_TEXT",
)

internal val resetControlActions = setOf(
    "RESET",
    "RESET_RECOGNITION",
    "RESET_SESSION",
)

internal val confirmControlActions = setOf(
    "CONFIRM",
    "COMMIT",
    "FINISH_INPUT",
    "SUBMIT",
)


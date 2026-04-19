package dev.forint.deafmute.cameraxdemo.tts

import android.speech.tts.TextToSpeech

enum class TtsUserStatusCode(val code: String) {
    READY("ready"),
    NO_ENGINE("no_engine"),
    INIT_TIMEOUT("init_timeout"),
    MISSING_LANGUAGE_DATA("missing_language_data"),
    LANGUAGE_NOT_SUPPORTED("language_not_supported"),
    ERROR("error"),
}

data class TtsStatusPresentation(
    val code: TtsUserStatusCode,
    val statusText: String,
    val reason: String,
    val suggestion: String,
)

object TtsStatusMapper {
    fun map(diagnostics: TtsDiagnostics): TtsStatusPresentation {
        if (diagnostics.ready) {
            return TtsStatusPresentation(
                code = TtsUserStatusCode.READY,
                statusText = "ready（可用）",
                reason = "系统 TTS 可正常工作，当前默认引擎支持播报。",
                suggestion = "可以直接使用播报功能。",
            )
        }

        if (diagnostics.phase == "timeout") {
            return TtsStatusPresentation(
                code = TtsUserStatusCode.INIT_TIMEOUT,
                statusText = "init_timeout（初始化超时）",
                reason = "系统 TTS 长时间没有返回初始化结果。",
                suggestion = "打开系统 TTS 设置检查默认引擎，必要时切换引擎后重试。",
            )
        }

        if (diagnostics.languageStatusCode == TextToSpeech.LANG_MISSING_DATA) {
            return TtsStatusPresentation(
                code = TtsUserStatusCode.MISSING_LANGUAGE_DATA,
                statusText = "missing_language_data（缺少语言数据）",
                reason = "当前 TTS 引擎缺少中文语音数据。",
                suggestion = "进入系统 TTS 设置下载或安装中文语音数据后重试。",
            )
        }

        if (diagnostics.languageStatusCode == TextToSpeech.LANG_NOT_SUPPORTED) {
            return TtsStatusPresentation(
                code = TtsUserStatusCode.LANGUAGE_NOT_SUPPORTED,
                statusText = "language_not_supported（语言不支持）",
                reason = "当前默认 TTS 引擎不支持中文播报。",
                suggestion = "切换到支持中文的 TTS 引擎，或安装支持中文的语音包。",
            )
        }

        if (diagnostics.phase == "creating" || diagnostics.phase == "waiting_on_init" || diagnostics.phase == "retrying") {
            return TtsStatusPresentation(
                code = TtsUserStatusCode.ERROR,
                statusText = "error（检测中）",
                reason = "正在等待系统 TTS 初始化结果。",
                suggestion = "如果长时间没有变化，请点“重试检测”或进入系统 TTS 设置检查引擎。",
            )
        }

        val noEngine = diagnostics.discoveredServices.isEmpty() &&
            diagnostics.installedEngines.isEmpty() &&
            diagnostics.defaultEngine == "--"
        if (noEngine) {
            return TtsStatusPresentation(
                code = TtsUserStatusCode.NO_ENGINE,
                statusText = "no_engine（未发现引擎）",
                reason = "设备没有发现可用的系统 TTS 引擎或默认引擎为空。",
                suggestion = "请先安装并启用系统语音引擎，再返回应用点“重试检测”。",
            )
        }

        return TtsStatusPresentation(
            code = TtsUserStatusCode.ERROR,
            statusText = "error（不可用）",
            reason = diagnostics.lastError ?: "系统 TTS 当前不可用，但没有返回更具体的错误。",
            suggestion = "请先打开系统 TTS 设置检查默认引擎和语言数据，再返回应用重试。",
        )
    }
}

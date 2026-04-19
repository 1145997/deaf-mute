package dev.forint.deafmute.cameraxdemo.domain

import dev.forint.deafmute.cameraxdemo.model.dto.PredictResponseDto

data class BufferMutation(
    val text: String,
    val bufferChanged: Boolean = false,
    val restartCountdown: Boolean = false,
    val immediateSpeakText: String? = null,
    val message: String? = null,
)

class SentenceBufferManager {
    private val tokens = mutableListOf<String>()
    private var lastOutputSignature: String = ""
    private var pendingTimeoutSpeakText: String? = null
    private var lastCommittedText: String = ""

    fun apply(result: PredictResponseDto): BufferMutation {
        val controlAction = result.controlAction.normalizeControlAction()
        if (controlAction in deleteControlActions) {
            if (tokens.isNotEmpty()) {
                tokens.removeAt(tokens.lastIndex)
            }
            resetCommitState()
            return BufferMutation(
                text = currentText(),
                bufferChanged = true,
                restartCountdown = currentText().isNotBlank(),
                message = "收到控制动作：$controlAction",
            )
        }

        if (controlAction in clearControlActions || controlAction in resetControlActions) {
            clearInternal()
            return BufferMutation(
                text = currentText(),
                bufferChanged = true,
                message = "收到控制动作：$controlAction",
            )
        }

        if (controlAction in confirmControlActions) {
            val speakText = resolvePreferredSpeakText(result)
            if (!speakText.isNullOrBlank()) {
                lastCommittedText = speakText
            }
            return BufferMutation(
                text = currentText(),
                immediateSpeakText = speakText,
                message = "收到确认动作：$controlAction",
            )
        }

        val outputType = result.outputType.orEmpty().trim().uppercase()
        val displayText = result.displayText.orEmpty().trim()
        if (result.locked == true || displayText.isBlank()) {
            return BufferMutation(text = currentText())
        }
        if (outputType != "TEXT" && outputType != "PHRASE") {
            return BufferMutation(text = currentText())
        }

        val outputSignature = buildOutputSignature(result)
        if (outputSignature.isNotBlank() && outputSignature == lastOutputSignature) {
            return BufferMutation(text = currentText())
        }
        lastOutputSignature = outputSignature

        tokens += displayText
        while (tokens.size > RecognitionConstants.MAX_BUFFER_TOKENS) {
            tokens.removeAt(0)
        }

        pendingTimeoutSpeakText = result.ttsText
            ?.takeIf { outputType == "PHRASE" }
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        lastCommittedText = ""

        return BufferMutation(
            text = currentText(),
            bufferChanged = true,
            restartCountdown = true,
            message = "追加输出：$displayText",
        )
    }

    fun clear(): BufferMutation {
        clearInternal()
        return BufferMutation(
            text = currentText(),
            bufferChanged = true,
            message = "本地句子缓冲已清空",
        )
    }

    fun timeoutCommitText(): String? {
        val candidate = pendingTimeoutSpeakText?.takeIf { it.isNotBlank() }
            ?: currentText().takeIf { it.isNotBlank() }
        if (candidate.isNullOrBlank() || candidate == lastCommittedText) {
            return null
        }
        lastCommittedText = candidate
        return candidate
    }

    fun currentText(): String = tokens.joinToString(separator = "")

    private fun resolvePreferredSpeakText(result: PredictResponseDto): String? {
        return result.ttsText?.trim()?.takeIf { it.isNotBlank() }
            ?: result.displayText?.trim()?.takeIf { it.isNotBlank() }
            ?: pendingTimeoutSpeakText?.takeIf { it.isNotBlank() }
            ?: currentText().takeIf { it.isNotBlank() }
    }

    private fun buildOutputSignature(result: PredictResponseDto): String {
        return listOf(
            result.traceId,
            result.matchedFlowCode,
            result.matchedNodeCode,
            result.code,
            result.displayText,
            result.controlAction,
        ).filter { !it.isNullOrBlank() }
            .joinToString(separator = "|")
    }

    private fun clearInternal() {
        tokens.clear()
        resetCommitState()
    }

    private fun resetCommitState() {
        pendingTimeoutSpeakText = null
        lastOutputSignature = ""
        lastCommittedText = ""
    }
}

private fun String?.normalizeControlAction(): String {
    return this.orEmpty().trim().uppercase()
}

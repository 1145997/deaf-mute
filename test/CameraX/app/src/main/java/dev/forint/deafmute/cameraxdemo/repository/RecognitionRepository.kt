package dev.forint.deafmute.cameraxdemo.repository

import dev.forint.deafmute.cameraxdemo.model.dto.BootstrapResponseDto
import dev.forint.deafmute.cameraxdemo.model.dto.PredictRequestDto
import dev.forint.deafmute.cameraxdemo.model.dto.PredictResponseDto
import dev.forint.deafmute.cameraxdemo.model.dto.SessionActionRequestDto
import dev.forint.deafmute.cameraxdemo.model.dto.SessionActionResponseDto
import dev.forint.deafmute.cameraxdemo.model.dto.SessionInfoDto
import dev.forint.deafmute.cameraxdemo.model.dto.SessionStartRequestDto
import dev.forint.deafmute.cameraxdemo.network.api.ApiEnvelope

class RecognitionRepository(
    baseUrl: String,
) {
    private val api = RecognitionApiFactory.create(baseUrl)

    suspend fun bootstrap(token: String): BootstrapResponseDto {
        return unwrap(api.bootstrap(token.toAuthorizationHeader()))
    }

    suspend fun startSession(token: String, request: SessionStartRequestDto): SessionInfoDto {
        return unwrap(api.startSession(token.toAuthorizationHeader(), request))
    }

    suspend fun predict(token: String, request: PredictRequestDto): PredictResponseDto {
        return unwrap(api.predict(token.toAuthorizationHeader(), request))
    }

    suspend fun closeSession(token: String, request: SessionActionRequestDto): SessionActionResponseDto {
        return unwrap(api.closeSession(token.toAuthorizationHeader(), request))
    }

    private fun <T> unwrap(envelope: ApiEnvelope<T>): T {
        if (envelope.code != 200) {
            throw IllegalStateException(envelope.message.ifBlank { "识别服务返回失败" })
        }
        return envelope.data ?: throw IllegalStateException("识别服务返回 data 为空")
    }
}

private fun String.toAuthorizationHeader(): String? {
    val raw = trim()
    if (raw.isEmpty()) {
        return null
    }
    return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
}


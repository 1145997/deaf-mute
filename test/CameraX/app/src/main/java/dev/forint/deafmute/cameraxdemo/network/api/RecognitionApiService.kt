package dev.forint.deafmute.cameraxdemo.network.api

import dev.forint.deafmute.cameraxdemo.model.dto.BootstrapResponseDto
import dev.forint.deafmute.cameraxdemo.model.dto.PredictRequestDto
import dev.forint.deafmute.cameraxdemo.model.dto.PredictResponseDto
import dev.forint.deafmute.cameraxdemo.model.dto.SessionActionRequestDto
import dev.forint.deafmute.cameraxdemo.model.dto.SessionActionResponseDto
import dev.forint.deafmute.cameraxdemo.model.dto.SessionInfoDto
import dev.forint.deafmute.cameraxdemo.model.dto.SessionStartRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface RecognitionApiService {
    @GET("recognition/bootstrap")
    suspend fun bootstrap(
        @Header("Authorization") authorization: String?,
    ): ApiEnvelope<BootstrapResponseDto>

    @POST("recognition/session/start")
    suspend fun startSession(
        @Header("Authorization") authorization: String?,
        @Body request: SessionStartRequestDto,
    ): ApiEnvelope<SessionInfoDto>

    @POST("recognition/predict")
    suspend fun predict(
        @Header("Authorization") authorization: String?,
        @Body request: PredictRequestDto,
    ): ApiEnvelope<PredictResponseDto>

    @POST("recognition/session/close")
    suspend fun closeSession(
        @Header("Authorization") authorization: String?,
        @Body request: SessionActionRequestDto,
    ): ApiEnvelope<SessionActionResponseDto>
}

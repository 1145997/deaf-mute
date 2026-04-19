package dev.forint.deafmute.cameraxdemo.network.api

data class ApiEnvelope<T>(
    val code: Int,
    val message: String,
    val data: T?,
)


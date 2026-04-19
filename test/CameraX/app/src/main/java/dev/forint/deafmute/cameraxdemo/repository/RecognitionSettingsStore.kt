package dev.forint.deafmute.cameraxdemo.repository

import android.content.Context
import dev.forint.deafmute.cameraxdemo.BuildConfig

data class RecognitionSettings(
    val baseUrl: String,
    val authToken: String,
)

class RecognitionSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun load(): RecognitionSettings {
        return RecognitionSettings(
            baseUrl = prefs.getString(KEY_BASE_URL, BuildConfig.DEFAULT_API_BASE_URL)
                ?: BuildConfig.DEFAULT_API_BASE_URL,
            authToken = prefs.getString(KEY_AUTH_TOKEN, "").orEmpty(),
        )
    }

    fun save(baseUrl: String, authToken: String) {
        prefs.edit()
            .putString(KEY_BASE_URL, baseUrl)
            .putString(KEY_AUTH_TOKEN, authToken)
            .apply()
    }

    companion object {
        private const val PREF_NAME = "recognition_demo_settings"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
}

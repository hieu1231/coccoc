package com.test.coccoc.config

import com.test.coccoc.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfig @Inject constructor() {
    val baseUrl: String = BuildConfig.BASE_URL
    val envName: String = BuildConfig.ENV_NAME
    val isDebug: Boolean = BuildConfig.DEBUG
    val versionName: String = BuildConfig.VERSION_NAME
    val versionCode: Int = BuildConfig.VERSION_CODE
    val applicationId: String = BuildConfig.APPLICATION_ID
    val geminiApiKey: String = BuildConfig.GEMINI_API_KEY

    val isProduction: Boolean
        get() = envName == "Production"

    val isDevelopment: Boolean
        get() = envName == "Development"

    val isStaging: Boolean
        get() = envName == "Staging"

    companion object {
        // Static access for non-DI contexts
        val BASE_URL: String = BuildConfig.BASE_URL
        val ENV_NAME: String = BuildConfig.ENV_NAME
        val IS_DEBUG: Boolean = BuildConfig.DEBUG
        val VERSION_NAME: String = BuildConfig.VERSION_NAME
        val VERSION_CODE: Int = BuildConfig.VERSION_CODE
        val APPLICATION_ID: String = BuildConfig.APPLICATION_ID
        val GEMINI_API_KEY: String = BuildConfig.GEMINI_API_KEY
    }
}

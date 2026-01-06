package com.test.coccoc.config

import com.test.coccoc.BuildConfig

object AppConfig {
    val BASE_URL: String = BuildConfig.BASE_URL
    val ENV_NAME: String = BuildConfig.ENV_NAME
    val IS_DEBUG: Boolean = BuildConfig.DEBUG
    val VERSION_NAME: String = BuildConfig.VERSION_NAME
    val VERSION_CODE: Int = BuildConfig.VERSION_CODE
    val APPLICATION_ID: String = BuildConfig.APPLICATION_ID

    val isProduction: Boolean
        get() = ENV_NAME == "Production"

    val isDevelopment: Boolean
        get() = ENV_NAME == "Development"

    val isStaging: Boolean
        get() = ENV_NAME == "Staging"
}

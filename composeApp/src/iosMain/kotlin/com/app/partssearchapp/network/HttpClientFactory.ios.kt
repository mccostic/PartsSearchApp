package com.app.partssearchapp.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createPlatformHttpClient(): HttpClient = HttpClient(Darwin)

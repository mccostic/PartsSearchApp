package com.app.partssearchapp.network

import io.ktor.client.HttpClient

expect fun createPlatformHttpClient(): HttpClient

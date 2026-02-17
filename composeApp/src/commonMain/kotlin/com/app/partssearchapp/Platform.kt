package com.app.partssearchapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
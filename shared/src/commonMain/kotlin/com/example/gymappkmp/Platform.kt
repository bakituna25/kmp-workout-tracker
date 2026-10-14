package com.example.gymappkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
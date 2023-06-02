package com.example.simpleotaclient.api

object ServerConfig {
    private const val SCHEME = "http"
    private const val HOST = "localhost"
    private const val PORT = "3000"
    const val API_URL = "$SCHEME://$HOST:$PORT"
}

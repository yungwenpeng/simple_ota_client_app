package com.example.simpleotaclient.websocket

object WebScoketServerConfig {
    private const val SCHEME = "ws"
    private const val HOST = "localhost"
    private const val PORT = "3000"
    const val WEBSOCKET_SERVER_URL = "$SCHEME://$HOST:$PORT"
}

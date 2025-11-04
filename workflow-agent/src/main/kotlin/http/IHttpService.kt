package org.example.http

interface HttpService {
    suspend fun get(path: String, params: Map<String, String> = emptyMap()): String
    fun close()
}
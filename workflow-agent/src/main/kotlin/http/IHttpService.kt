package org.example.http

interface IHttpService {
    suspend fun get(url: String, params: Map<String, String> = emptyMap()): String
    fun close()
}
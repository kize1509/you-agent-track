package org.example.http

interface IHttpService {
    suspend fun get(path: String, params: Map<String, String> = emptyMap()): String
    fun close()
}
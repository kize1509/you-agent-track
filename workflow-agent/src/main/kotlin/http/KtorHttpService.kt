package org.example.http

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class KtorHttpService(private val baseUrl: String, private val token: String) : IHttpService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; prettyPrint = true })
        }
    }

    override suspend fun get(path: String, params: Map<String, String>): String {
        val response = client.get("$baseUrl$path") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            params.forEach { (k, v) -> parameter(k, v) }
        }
        return response.body()
    }

    override fun close() = client.close()
}
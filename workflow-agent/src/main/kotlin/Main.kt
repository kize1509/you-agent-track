package org.example

import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import org.example.config.YouTrackConfig
import org.example.factory.DependencyFactory

fun main() = runBlocking {
    println("=== YouTrack Workflow Analysis Agent ===\n")
    
    val config = loadConfiguration()
    
    val factory = DependencyFactory(
        youTrackConfig = config.first,
        openAiApiKey = config.second
    )
    
    val agent = factory.createAgent()
    
    println("Agent is ready.")

    print("Your query: ")
    val userQuery = readLine() ?: run {
        println("No input provided. Exiting.")
        return@runBlocking
    }
    
    if (userQuery.isBlank()) {
        println("Empty query. Exiting.")
        return@runBlocking
    }
    
    println("\nAnalyzing... This may take a moment.\n")
    
    try {
        val result = agent.analyze(userQuery)
        
        println("=== Analysis Result ===")
        println(result)
    } catch (e: Exception) {
        println("Error during analysis: ${e.message}")
        e.printStackTrace()
    }
}


private fun loadConfiguration(): Pair<YouTrackConfig, String> {
    val dotenv = dotenv {
        directory = "./"
        ignoreIfMissing = true
    }
    
    val youTrackBaseUrl = dotenv["YOUTRACK_BASE_URL"]
        ?: System.getenv("YOUTRACK_BASE_URL")
        ?: throw IllegalStateException("""
            YOUTRACK_BASE_URL is required.
            
            Either:
            1. Create a .env file in the project root with:
               YOUTRACK_BASE_URL=https://your-youtrack-instance.com
            
            2. Or set environment variable:
               export YOUTRACK_BASE_URL="https://your-youtrack-instance.com"
        """.trimIndent())
    
    val youTrackToken = dotenv["YOUTRACK_TOKEN"]
        ?: System.getenv("YOUTRACK_TOKEN")
        ?: throw IllegalStateException("""
            YOUTRACK_TOKEN is required.
            
            Either:
            1. Add to .env file:
               YOUTRACK_TOKEN=perm:your-token-here
            
            2. Or set environment variable:
               export YOUTRACK_TOKEN="perm:your-token-here"
        """.trimIndent())
    
    val openAiApiKey = dotenv["OPENAI_API_KEY"]
        ?: System.getenv("OPENAI_API_KEY")
        ?: throw IllegalStateException("""
            OPENAI_API_KEY is required.
            
            Either:
            1. Add to .env file:
               OPENAI_API_KEY=sk-your-key-here
            
            2. Or set environment variable:
               export OPENAI_API_KEY="sk-your-key-here"
        """.trimIndent())
    
    println("Configuration loaded successfully:")
    println("  YouTrack URL: $youTrackBaseUrl")
    println("  YouTrack Token: ${youTrackToken.take(8)}...")
    println("  OpenAI API Key: ${openAiApiKey.take(8)}...")
    println()
    
    return Pair(
        YouTrackConfig(baseUrl = youTrackBaseUrl, token = youTrackToken),
        openAiApiKey
    )
}
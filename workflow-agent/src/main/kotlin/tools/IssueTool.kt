package org.example.tools

import ai.koog.agents.core.tools.Tool
import kotlinx.serialization.KSerializer
import org.example.service.YouTrackService

class IssueTool(private val service: YouTrackService) : Tool<String, String>() {
    override val argsSerializer: KSerializer<String>
        get() = TODO("Not yet implemented")
    override val resultSerializer: KSerializer<String>
        get() = TODO("Not yet implemented")
    override val description: String
        get() = TODO("Not yet implemented")

    override suspend fun execute(args: String): String = service.getIssueDetails(args)

}
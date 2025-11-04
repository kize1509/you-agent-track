package org.example.tools

import ai.koog.agents.core.tools.Tool
import kotlinx.serialization.KSerializer
import org.example.model.Workflow
import org.example.service.YouTrackService

class WorkflowsTool(private val service: YouTrackService) : Tool<String, List<Workflow>>() {
    override val argsSerializer: KSerializer<String>
        get() = TODO("Not yet implemented")
    override val resultSerializer: KSerializer<List<Workflow>>
        get() = TODO("Not yet implemented")
    override val description: String
        get() = TODO("Not yet implemented")

    override suspend fun execute(args: String): List<Workflow> = service.getProjectWorkflows(args)

}
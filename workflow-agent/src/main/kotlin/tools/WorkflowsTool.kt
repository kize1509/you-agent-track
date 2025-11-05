package org.example.tools

import ai.koog.agents.core.tools.Tool
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.example.model.Workflow
import org.example.service.YouTrackService

@Serializable
data class WorkflowsToolArgs(val projectId: String)

class WorkflowsTool(private val service: YouTrackService) : Tool<WorkflowsToolArgs, String>() {
    
    override val name: String = "get_project_workflows"
    
    override val description: String = """
        Get all workflows configured for a specific YouTrack project.
        Returns a list of workflows with their IDs and names.
    """.trimIndent()
    
    override val argsSerializer: KSerializer<WorkflowsToolArgs> = WorkflowsToolArgs.serializer()
    
    override val resultSerializer: KSerializer<String> = String.serializer()

    public override suspend fun execute(args: WorkflowsToolArgs): String {
        val workflows = service.getProjectWorkflows(args.projectId)

        return workflows.joinToString("\n\n") { workflow ->
            """
            Workflow: ${workflow.title ?: workflow.name ?: workflow.id}
            ID: ${workflow.id}
            Rules count: ${workflow.rules.size}
            """.trimIndent()
        }
    }
}
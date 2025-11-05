package org.example.tools


import ai.koog.agents.core.tools.Tool
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import org.example.service.YouTrackService


@Serializable
data class WorkflowRulesToolArgs(val workflowId: String)

class WorkflowRulesTool(private val service: YouTrackService) : Tool<WorkflowRulesToolArgs, String>() {

    override val name: String = "get_workflow_rules"

    override val description: String = "Get all rules for a specific workflow"

    override val argsSerializer: KSerializer<WorkflowRulesToolArgs> = WorkflowRulesToolArgs.serializer()

    override val resultSerializer: KSerializer<String> = String.serializer()

    public override suspend fun execute(args: WorkflowRulesToolArgs): String {
        val rules = service.getRules(args.workflowId)

        return rules.joinToString("\n\n") { rule ->
            buildString {
                appendLine("Rule: ${rule.title ?: rule.name ?: rule.id}")
                appendLine("ID: ${rule.id}") 
                appendLine("Type: ${rule.type ?: "Unknown"}")
                rule.description?.let { appendLine("Description: $it") }
                appendLine("ReadOnly: ${rule.readOnly ?: false}")
                appendLine("Enabled: ${rule.enabled ?: true}")
                rule.script?.let { 
                    appendLine("Script Preview: ${it.take(200)}${if (it.length > 200) "..." else ""}")
                }
            }.trimEnd()
        }
    }
}
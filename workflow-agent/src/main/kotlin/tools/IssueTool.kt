package org.example.tools

import ai.koog.agents.core.tools.Tool
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.example.service.YouTrackService

@Serializable
data class IssueToolArgs(val issueId: String)

class IssueTool(private val service: YouTrackService) : Tool<IssueToolArgs, String>() {
    
    override val name: String = "get_issue_details"
    
    override val description: String = """
        Get detailed information about a specific YouTrack issue by its ID.
        Returns the issue's summary, description, and custom field values.
    """.trimIndent()
    
    override val argsSerializer: KSerializer<IssueToolArgs> = IssueToolArgs.serializer()
    
    override val resultSerializer: KSerializer<String> = String.serializer()

    public override suspend fun execute(args: IssueToolArgs): String = service.getIssueDetails(args.issueId)

}
package org.example.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResult
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import org.example.tools.IssueTool
import org.example.tools.WorkflowRulesTool
import org.example.tools.WorkflowsTool

class WorkflowAnalysisAgent(
    private val issueTool: IssueTool,
    private val workflowsTool: WorkflowsTool,
    private val workflowRulesTool: WorkflowRulesTool,
    private val apiKey: String
) {

    private fun createStrategy() = strategy<String, String>("workflow-analysis") {
        val nodeCallLLM by nodeLLMRequest()
        val executeToolCall by nodeExecuteTool()
        val sendToolResult by nodeLLMSendToolResult()
        
        edge(nodeStart forwardTo nodeCallLLM)
        edge(nodeCallLLM forwardTo nodeFinish onAssistantMessage { true })
        edge(nodeCallLLM forwardTo executeToolCall onToolCall { true })
        edge(executeToolCall forwardTo sendToolResult)
        edge(sendToolResult forwardTo nodeFinish onAssistantMessage { true })
        edge(sendToolResult forwardTo executeToolCall onToolCall { true })
    }
    

    val agent = AIAgent(
            promptExecutor = simpleOpenAIExecutor(apiKey),
            systemPrompt = """
                You are a YouTrack workflow analysis expert. Your job is to help users understand why their actions 
                were rejected by workflow rules.
                
                When a user describes an issue:
                1. Extract the issue ID from their description (e.g., DEMO-42)
                2. Extract the project ID from the issue ID (e.g., DEMO from DEMO-42)
                3. Use get_issue_details to fetch the issue information
                4. Use get_project_workflows to get all workflows for the project
                5. For each workflow, use get_workflow_rules to get detailed rules
                6. Analyze the rules to find which ones might have caused the rejection
                7. Provide a clear explanation of:
                   - Why the action was rejected
                   - Which workflow rule(s) caused the rejection
                   - What conditions in the rule were triggered
                   - How to resolve the issue (if possible)
                
                Be specific and provide helpful information with references to the workflow rules.
            """.trimIndent(),
            llmModel = OpenAIModels.Chat.GPT4o,
            temperature = 0.1,
            toolRegistry = ToolRegistry {
                tool(issueTool)
                tool(workflowsTool)
                tool(workflowRulesTool)
            },
            strategy = createStrategy(),
            maxIterations = 20
        )

    suspend fun analyze(userDescription: String): String {
        val result = agent.run(userDescription)
        return result.toString()
    }
}


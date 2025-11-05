package org.example.factory

import org.example.agent.WorkflowAnalysisAgent
import org.example.api.IYouTrackApi
import org.example.api.YouTrackApiImpl
import org.example.config.YouTrackConfig
import org.example.http.IHttpService
import org.example.http.KtorHttpService
import org.example.service.YouTrackService
import org.example.tools.IssueTool
import org.example.tools.WorkflowRulesTool
import org.example.tools.WorkflowsTool


class DependencyFactory(
    private val youTrackConfig: YouTrackConfig,
    private val openAiApiKey: String
) {
    

    fun createHttpService(): IHttpService {
        return KtorHttpService(
            baseUrl = youTrackConfig.baseUrl,
            token = youTrackConfig.token
        )
    }
    

    fun createYouTrackApi(): IYouTrackApi {
        val httpService = createHttpService()
        return YouTrackApiImpl(httpService)
    }

    fun createYouTrackService(): YouTrackService {
        val api = createYouTrackApi()
        return YouTrackService(api)
    }
    

    fun createTools(): Triple<IssueTool, WorkflowsTool, WorkflowRulesTool> {
        val service = createYouTrackService()
        return Triple(
            IssueTool(service),
            WorkflowsTool(service),
            WorkflowRulesTool(service)
        )
    }

    fun createAgent(): WorkflowAnalysisAgent {
        val (issueTool, workflowsTool, workflowRulesTool) = createTools()
        return WorkflowAnalysisAgent(
            issueTool = issueTool,
            workflowsTool = workflowsTool,
            workflowRulesTool = workflowRulesTool,
            apiKey = openAiApiKey
        )
    }
}


package org.example.api

import kotlinx.serialization.json.Json
import org.example.http.IHttpService
import org.example.model.Workflow
import org.example.model.WorkflowRule

class YouTrackApiImpl(private val http: IHttpService) : IYouTrackApi {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getWorkflows(projectId: String): List<Workflow> =
        json.decodeFromString(http.get("/api/admin/projects/$projectId/workflows"))

    override suspend fun getWorkflowRules(workflowId: String): List<WorkflowRule> =
        json.decodeFromString(http.get("/api/admin/workflows/$workflowId/rules"))

    override suspend fun getIssue(issueId: String): String =
        http.get("/api/issues/$issueId", mapOf("fields" to "id,summary,description,customFields(name,value)"))
}
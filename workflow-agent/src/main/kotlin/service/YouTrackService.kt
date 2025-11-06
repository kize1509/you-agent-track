package org.example.service

import org.example.api.IYouTrackApi

class YouTrackService(private val api: IYouTrackApi) {
    suspend fun getProjectWorkflows(projectId: String) = api.getWorkflows(projectId)
    suspend fun getRules(workflowId: String) = api.getWorkflowRules(workflowId)
    suspend fun getIssueDetails(issueId: String) = api.getIssue(issueId)
}
package org.example.api

import org.example.model.Workflow
import org.example.model.WorkflowRule

interface IYouTrackApi {
    suspend fun getWorkflows(projectId: String): List<Workflow>
    suspend fun getWorkflowRules(workflowId: String): List<WorkflowRule>
    suspend fun getIssue(issueId: String): String
}
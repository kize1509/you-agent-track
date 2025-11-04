package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Workflow(
    val id: String,
    val name: String,
    val rules: List<WorkflowRule> = emptyList()
)

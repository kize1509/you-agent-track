package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Workflow(
    val id: String,
    val title: String? = null,
    val name: String? = null,
    val rules: List<WorkflowRule> = emptyList()
)

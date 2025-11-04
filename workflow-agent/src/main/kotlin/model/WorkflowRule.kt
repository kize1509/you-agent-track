package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkflowRule(
    val id: String,
    val name: String,
    val description: String? = null,
    val type: String
)
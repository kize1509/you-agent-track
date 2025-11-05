package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkflowRule(
    val id: String,
    val name: String? = null,
    val title: String? = null,
    val description: String? = null,
    val type: String? = null,
    val readOnly: Boolean? = null,
    val script: String? = null,
    val text: String? = null,
    val enabled: Boolean? = null
)
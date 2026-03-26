package com.esports.space.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class AgentRulesResponse(
    val rules: List<Map<String, Any>>,
    val version: Int,
    @Json(name = "min_app_version") val minAppVersion: String,
)

@JsonClass(generateAdapter = true)
data class AgentEnhanceRequest(
    val dimensions: List<String>? = null,
    val behaviorSummary: String? = null,
)

@JsonClass(generateAdapter = true)
data class AgentEnhanceResponse(
    val recommendations: List<String>? = null,
)

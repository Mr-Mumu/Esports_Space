package com.esports.space.network.api

import com.esports.space.network.model.AgentEnhanceRequest
import com.esports.space.network.model.AgentEnhanceResponse
import com.esports.space.network.model.AgentRulesResponse
import com.esports.space.network.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AgentApi {
    @GET("v1/agent/rules")
    suspend fun getRules(): ApiResponse<AgentRulesResponse>

    @POST("v1/agent/enhance")
    suspend fun enhance(@Body body: AgentEnhanceRequest): ApiResponse<AgentEnhanceResponse>
}

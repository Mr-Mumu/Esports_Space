package com.esports.space.network.api

import com.esports.space.network.model.ApiResponse
import com.esports.space.network.model.LiveItem
import retrofit2.http.GET

interface LiveApi {
    @GET("v1/live")
    suspend fun getLive(): ApiResponse<List<LiveItem>>
}

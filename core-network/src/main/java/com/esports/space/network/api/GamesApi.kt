package com.esports.space.network.api

import com.esports.space.network.model.ApiResponse
import com.esports.space.network.model.NewGameItem
import retrofit2.http.GET

interface GamesApi {
    @GET("v1/games/new")
    suspend fun getNewGames(): ApiResponse<List<NewGameItem>>

    @GET("v1/games/whitelist")
    suspend fun getWhitelist(): ApiResponse<List<String>>
}

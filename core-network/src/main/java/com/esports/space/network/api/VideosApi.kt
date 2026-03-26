package com.esports.space.network.api

import com.esports.space.network.model.ApiResponse
import com.esports.space.network.model.VideoItem
import retrofit2.http.GET
import retrofit2.http.Query

interface VideosApi {
    @GET("v1/videos")
    suspend fun getVideos(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("game_filter") gameFilter: String?,
    ): ApiResponse<List<VideoItem>>
}

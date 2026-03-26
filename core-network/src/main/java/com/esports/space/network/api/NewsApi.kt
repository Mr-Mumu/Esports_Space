package com.esports.space.network.api

import com.esports.space.network.model.ApiResponse
import com.esports.space.network.model.NewsPageData
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("v1/news")
    suspend fun getNews(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("game_filter") gameFilter: String?,
    ): ApiResponse<NewsPageData>
}

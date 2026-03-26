package com.esports.space.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class ApiResponse<T>(
    val code: Int,
    val message: String? = null,
    val data: T? = null,
)

@JsonClass(generateAdapter = true)
data class NewsPageData(
    val total: Int,
    val items: List<NewsItem>,
)

@JsonClass(generateAdapter = true)
data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "detail_url") val detailUrl: String,
    @Json(name = "published_at") val publishedAt: String,
    val tags: List<String>,
    @Json(name = "is_live") val isLive: Boolean,
    @Json(name = "live_url") val liveUrl: String? = null,
)

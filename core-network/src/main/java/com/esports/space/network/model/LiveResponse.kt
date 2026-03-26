package com.esports.space.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LiveItem(
    val platform: String,
    val streamer: String,
    @Json(name = "viewer_count") val viewerCount: Int,
    @Json(name = "stream_url") val streamUrl: String,
    @Json(name = "deep_link") val deepLink: String,
    @Json(name = "thumbnail_url") val thumbnailUrl: String,
    val tags: List<String>,
)

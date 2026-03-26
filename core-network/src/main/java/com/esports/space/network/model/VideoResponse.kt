package com.esports.space.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoItem(
    @Json(name = "video_url") val videoUrl: String,
    @Json(name = "thumbnail_url") val thumbnailUrl: String,
    @Json(name = "duration_seconds") val durationSeconds: Int,
    val tags: List<String>,
)

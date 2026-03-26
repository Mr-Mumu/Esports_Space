package com.esports.space.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewGameItem(
    @Json(name = "package_name") val packageName: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "icon_url") val iconUrl: String,
    @Json(name = "poster_url") val posterUrl: String,
    val description: String,
    @Json(name = "store_url") val storeUrl: String,
    val tags: List<String>,
)

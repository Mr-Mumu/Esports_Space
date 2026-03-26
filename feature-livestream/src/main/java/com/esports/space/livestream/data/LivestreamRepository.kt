package com.esports.space.livestream.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.esports.space.network.api.LiveApi
import com.esports.space.network.api.VideosApi
import com.esports.space.network.model.LiveItem
import com.esports.space.network.model.VideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LivestreamRepository @Inject constructor(
    private val liveApi: LiveApi,
    private val videosApi: VideosApi,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _liveStreams = MutableStateFlow<List<LiveItem>>(emptyList())
    private val _videoHighlights = MutableStateFlow<List<VideoItem>>(emptyList())

    fun liveStreams(): Flow<List<LiveItem>> = _liveStreams.asStateFlow()

    fun videoHighlights(): Flow<List<VideoItem>> = _videoHighlights.asStateFlow()

    init {
        scope.launch {
            refreshLive()
            while (isActive) {
                delay(5 * 60 * 1000L)
                refreshLive()
            }
        }
    }

    suspend fun refreshLive() {
        runCatching {
            val response = liveApi.getLive()
            val list = response.data
            if (list != null) {
                _liveStreams.value = list
            }
        }
    }

    suspend fun refreshVideos(page: Int = 1, pageSize: Int = 20) {
        runCatching {
            val response = videosApi.getVideos(page, pageSize, null)
            val list = response.data
            if (list != null) {
                _videoHighlights.value = list
            }
        }
    }

    fun resolveDeepLink(liveItem: LiveItem, context: Context): Intent? {
        val uri = runCatching { Uri.parse(liveItem.deepLink.trim()) }.getOrNull() ?: return null
        if (uri.scheme.isNullOrBlank()) return null
        val intent = Intent(Intent.ACTION_VIEW, uri)
        return if (intent.resolveActivity(context.packageManager) != null) intent else null
    }
}

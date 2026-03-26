package com.esports.space.news.data

import com.esports.space.network.api.LiveApi
import com.esports.space.network.api.NewsApi
import com.esports.space.network.model.LiveItem
import com.esports.space.network.model.NewsItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val newsApi: NewsApi,
    private val liveApi: LiveApi,
) {

    companion object {
        private const val CACHE_MAX = 20
    }

    private val _cachedNews = MutableStateFlow<List<NewsItem>>(emptyList())
    private val _cachedLive = MutableStateFlow<List<LiveItem>>(emptyList())

    fun newsFlow(): Flow<List<NewsItem>> = _cachedNews.asStateFlow()

    fun liveStreams(): Flow<List<LiveItem>> = _cachedLive.asStateFlow()

    suspend fun refresh() {
        runCatching {
            val response = newsApi.getNews(page = 1, pageSize = CACHE_MAX, gameFilter = null)
            val page = response.data
            if (page != null) {
                _cachedNews.value = page.items.take(CACHE_MAX)
            }
        }
        runCatching {
            val response = liveApi.getLive()
            val list = response.data
            if (list != null) {
                _cachedLive.value = list
            }
        }
    }
}

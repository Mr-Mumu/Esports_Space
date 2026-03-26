package com.esports.space.news.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.space.network.model.LiveItem
import com.esports.space.network.model.NewsItem
import com.esports.space.news.data.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewsUiState(
    val newsList: List<NewsItem> = emptyList(),
    val liveStreams: List<LiveItem> = emptyList(),
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.newsFlow(), repository.liveStreams()) { news, live ->
                news to live
            }.collect { (news, live) ->
                _uiState.update { it.copy(newsList = news, liveStreams = live) }
            }
        }
        viewModelScope.launch {
            while (isActive) {
                repository.refresh()
                delay(30 * 60 * 1000L)
            }
        }
    }

    fun manualRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                repository.refresh()
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }
}

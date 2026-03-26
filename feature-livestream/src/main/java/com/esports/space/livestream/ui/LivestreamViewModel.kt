package com.esports.space.livestream.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.space.network.model.LiveItem
import com.esports.space.network.model.VideoItem
import com.esports.space.livestream.data.LivestreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LivestreamUiState(
    val liveStreams: List<LiveItem> = emptyList(),
    val videoHighlights: List<VideoItem> = emptyList(),
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class LivestreamViewModel @Inject constructor(
    private val repository: LivestreamRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LivestreamUiState())
    val uiState: StateFlow<LivestreamUiState> = _uiState.asStateFlow()

    private val _openWebViewUrl = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val openWebViewUrl: SharedFlow<String> = _openWebViewUrl.asSharedFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                repository.refreshLive()
                repository.refreshVideos()
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
        viewModelScope.launch {
            combine(
                repository.liveStreams(),
                repository.videoHighlights(),
            ) { live, videos ->
                live to videos
            }.collect { (live, videos) ->
                _uiState.update { it.copy(liveStreams = live, videoHighlights = videos) }
            }
        }
    }

    fun onStreamClick(item: LiveItem, context: Context) {
        val intent = repository.resolveDeepLink(item, context)
        if (intent != null) {
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                viewModelScope.launch { _openWebViewUrl.emit(item.streamUrl) }
            }
        } else {
            viewModelScope.launch { _openWebViewUrl.emit(item.streamUrl) }
        }
    }
}

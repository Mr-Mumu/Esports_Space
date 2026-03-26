package com.esports.space.performance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.space.data.db.entity.DeviceSnapshotEntity
import com.esports.space.performance.data.DeviceMetrics
import com.esports.space.performance.data.PerformanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerformanceUiState(
    val currentMetrics: DeviceMetrics? = null,
    val cpuFreqHistory: List<Float> = emptyList(),
    val gpuFreqHistory: List<Float> = emptyList(),
    val recentSnapshots: List<DeviceSnapshotEntity> = emptyList()
)

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    private val repository: PerformanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerformanceUiState())
    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    private val cpuBuffer = ArrayDeque<Float>(PerformanceRepository.ECG_BUFFER_SIZE)
    private val gpuBuffer = ArrayDeque<Float>(PerformanceRepository.ECG_BUFFER_SIZE)

    init {
        viewModelScope.launch {
            repository.recentSnapshots(1).collect { list ->
                _uiState.value = _uiState.value.copy(recentSnapshots = list)
            }
        }
        viewModelScope.launch {
            repository.realtimeMetrics().collect { metrics ->
                metrics.cpuFreqMhz?.let { freq ->
                    if (cpuBuffer.size >= PerformanceRepository.ECG_BUFFER_SIZE) cpuBuffer.removeFirst()
                    cpuBuffer.addLast(freq.toFloat())
                }
                metrics.gpuFreqMhz?.let { freq ->
                    if (gpuBuffer.size >= PerformanceRepository.ECG_BUFFER_SIZE) gpuBuffer.removeFirst()
                    gpuBuffer.addLast(freq.toFloat())
                }

                _uiState.value = _uiState.value.copy(
                    currentMetrics = metrics,
                    cpuFreqHistory = cpuBuffer.toList(),
                    gpuFreqHistory = gpuBuffer.toList()
                )

                repository.saveSnapshot(metrics)
            }
        }
    }
}

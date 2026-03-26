package com.esports.space.datacenter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.space.datacenter.data.DailyPlayStat
import com.esports.space.datacenter.data.GameTimeStat
import com.esports.space.datacenter.data.HourlyHeatStat
import com.esports.space.datacenter.data.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DataCenterUiState(
    val todayMs: Long = 0L,
    val weekDays: Int = 0,
    val monthMs: Long = 0L,
    val dailyHistory: List<DailyPlayStat> = emptyList(),
    val gameDistribution: List<GameTimeStat> = emptyList(),
    val heatmapData: List<HourlyHeatStat> = emptyList(),
    val healthTip: String = "",
)

@HiltViewModel
class DataCenterViewModel @Inject constructor(
    private val statsRepo: StatsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataCenterUiState())
    val uiState: StateFlow<DataCenterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                statsRepo.gameTimeDistribution(),
                statsRepo.dailyPlayHistory(7),
                statsRepo.hourlyHeatmap(),
            ) { distribution, daily, heat ->
                Triple(distribution, daily, heat)
            }.collectLatest { (distribution, daily, heat) ->
                val today = statsRepo.getTodayPlayTime()
                _uiState.value = DataCenterUiState(
                    todayMs = today,
                    weekDays = statsRepo.getWeeklyGameDays(),
                    monthMs = statsRepo.getMonthPlayTime(),
                    dailyHistory = daily,
                    gameDistribution = distribution,
                    heatmapData = heat,
                    healthTip = healthTipFor(today),
                )
            }
        }
    }
}

private fun healthTipFor(todayMs: Long): String {
    val hours = todayMs / 3_600_000f
    return when {
        todayMs == 0L -> "今日尚未记录游戏，放松一下或开始游戏吧。"
        hours < 1f -> "今日游戏时间较少，注意休息与屏幕距离。"
        hours < 3f -> "今日游戏时间适中，继续保持！"
        hours < 5f -> "今日游戏时间较长，建议适当休息。"
        else -> "今日游戏时间偏长，请适度休息保护视力。"
    }
}

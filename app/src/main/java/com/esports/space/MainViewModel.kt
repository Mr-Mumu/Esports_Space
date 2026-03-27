package com.esports.space

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.space.data.datastore.UserPreferenceStore
import com.esports.space.ui.theme.GalaxyThemeConfig
import com.esports.space.ui.theme.LuxuryThemeConfig
import com.esports.space.ui.theme.NeonTechThemeConfig
import com.esports.space.ui.theme.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceStore: UserPreferenceStore
) : ViewModel() {

    val currentTheme: StateFlow<ThemeConfig> = preferenceStore.theme
        .map { id -> resolveTheme(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GalaxyThemeConfig)

    val permissionsChecked = MutableStateFlow(false)

    fun switchTheme(themeId: String) {
        viewModelScope.launch { preferenceStore.setTheme(themeId) }
    }

    private fun resolveTheme(id: String): ThemeConfig = when (id) {
        "neon_tech" -> NeonTechThemeConfig
        "luxury" -> LuxuryThemeConfig
        else -> GalaxyThemeConfig
    }
}

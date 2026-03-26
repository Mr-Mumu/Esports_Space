package com.esports.space.games.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class ScannedGame(
    val packageName: String,
    val displayName: String,
    val iconUri: String
)

class GameScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    @Suppress("DEPRECATION")
    fun scanInstalledGames(whitelist: Set<String>): List<ScannedGame> {
        val pm = context.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { app ->
                app.packageName in whitelist ||
                        (android.os.Build.VERSION.SDK_INT >= 26 &&
                                app.category == ApplicationInfo.CATEGORY_GAME)
            }
            .mapNotNull { app ->
                val label = pm.getApplicationLabel(app)?.toString() ?: return@mapNotNull null
                val iconRes = app.icon
                val iconUri = if (iconRes != 0) {
                    "android.resource://${app.packageName}/$iconRes"
                } else {
                    ""
                }
                ScannedGame(
                    packageName = app.packageName,
                    displayName = label,
                    iconUri = iconUri
                )
            }
    }
}

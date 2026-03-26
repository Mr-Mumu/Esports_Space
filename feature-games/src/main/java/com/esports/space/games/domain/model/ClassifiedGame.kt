package com.esports.space.games.domain.model

import com.esports.space.data.db.entity.GameCategory

data class ClassifiedGame(
    val packageName: String,
    val displayName: String,
    val iconUri: String,
    val posterUri: String?,
    val category: GameCategory,
    val score: Double,
    val isNewRelease: Boolean = false,
    val storeUrl: String? = null
)

package com.esports.space.data.db.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameRecordEntityTest {
    @Test
    fun `entity creation with all fields`() {
        val record = GameRecordEntity(
            packageName = "com.tencent.tmgp.sgame",
            displayName = "王者荣耀",
            iconUri = "content://icon",
            posterUri = null,
            category = GameCategory.FREQUENT,
            totalPlayTime = 3600000L,
            lastPlayedAt = 1711000000000L,
            launchCount = 42,
            pinned = false
        )
        assertEquals("com.tencent.tmgp.sgame", record.packageName)
        assertEquals(GameCategory.FREQUENT, record.category)
        assertNull(record.posterUri)
        assertEquals(42, record.launchCount)
    }
}

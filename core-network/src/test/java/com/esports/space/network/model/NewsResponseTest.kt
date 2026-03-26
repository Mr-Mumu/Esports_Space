package com.esports.space.network.model

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsResponseTest {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @Test
    fun `deserialize news item from json`() {
        val json = """{"id":"news_001","title":"KPL决赛","summary":"...","source":"腾讯电竞","image_url":"https://img","detail_url":"https://detail","published_at":"2026-03-26T14:00:00Z","tags":["KPL"],"is_live":true,"live_url":"https://live"}"""
        val adapter = moshi.adapter(NewsItem::class.java)
        val item = adapter.fromJson(json)!!
        assertEquals("news_001", item.id)
        assertTrue(item.isLive)
        assertEquals("KPL决赛", item.title)
    }
}

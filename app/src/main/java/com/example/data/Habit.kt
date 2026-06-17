package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Habit(
    val id: String,
    val username: String,
    val title: String,
    val description: String = "",
    val daysDone: List<String> = emptyList(), // Stores completion dates: e.g. "2026-06-17"
    val createdAt: Long = System.currentTimeMillis()
)

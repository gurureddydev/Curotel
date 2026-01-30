package com.app.curotel.domain.model

data class ChatMessage(
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val isSelf: Boolean
)

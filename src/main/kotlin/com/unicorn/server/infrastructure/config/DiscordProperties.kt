package com.unicorn.server.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.discord")
data class DiscordProperties(
    val enabled: Boolean,
    val botToken: String,
    val errorChannelId: String,
    val apiBaseUrl: String,
    val maxMessageLength: Int,
)

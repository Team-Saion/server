package com.unicorn.server.infrastructure.adapter.out.discord

import com.unicorn.server.common.port.out.alert.AlertOutPort
import com.unicorn.server.infrastructure.config.DiscordProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class DiscordNotificationAdapter(
    private val discordProperties: DiscordProperties,
    private val discordRestTemplate: RestTemplate,
) : AlertOutPort {

    override fun sendErrorAlert(e: Exception) {
        if (!discordProperties.enabled) return

        val url = "${discordProperties.apiBaseUrl}/channels/${discordProperties.errorChannelId}/messages"
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("Authorization", "Bot ${discordProperties.botToken}")
        }
        val body = mapOf("content" to buildMessage(e).take(discordProperties.maxMessageLength))
        val request = HttpEntity(body, headers)

        runCatching { discordRestTemplate.postForEntity(url, request, String::class.java) }
            .onFailure { log.error("[Discord] 에러 알림 전송 실패", it) }
    }

    private fun buildMessage(e: Exception): String =
        """
        🚨 **Server Error**
        **예외**: `${e.javaClass.simpleName}`
        **메시지**: ${e.message ?: "N/A"}
        **스택 상단**:
        ```
        ${e.stackTrace.take(5).joinToString("\n")}
        ```
        """.trimIndent()

    companion object {
        private val log = LoggerFactory.getLogger(DiscordNotificationAdapter::class.java)
    }
}

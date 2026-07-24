package com.unicorn.server.infrastructure.adapter.out.discord

import com.unicorn.server.common.port.out.alert.ErrorAlertPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class DiscordNotificationAdapter(
    @Value("\${app.discord.enabled}") private val enabled: Boolean,
    @Value("\${app.discord.bot-token}") private val botToken: String,
    @Value("\${app.discord.error-channel-id}") private val errorChannelId: String,
    @Value("\${app.discord.api-base-url}") private val apiBaseUrl: String,
    @Value("\${app.discord.max-message-length}") private val maxMessageLength: Int,
) : ErrorAlertPort {

    private val restTemplate = RestTemplate()

    override fun sendErrorAlert(e: Exception) {
        if (!enabled) return

        val url = "$apiBaseUrl/channels/$errorChannelId/messages"
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("Authorization", "Bot $botToken")
        }
        val body = mapOf("content" to buildMessage(e).take(maxMessageLength))
        val request = HttpEntity(body, headers)

        runCatching { restTemplate.postForEntity(url, request, String::class.java) }
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

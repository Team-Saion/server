package com.unicorn.server.domain.notification

import com.unicorn.server.domain.notification.enums.DevicePlatform
import com.unicorn.server.domain.notification.vo.DevicePushTokenId
import java.time.LocalDateTime

class DevicePushToken private constructor(
    val id: DevicePushTokenId?,
    var memberId: String,
    installationId: String,
    token: String,
    platform: DevicePlatform,
    active: Boolean,
    lastSeenAt: LocalDateTime,
    invalidatedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
) {
    var installationId: String = installationId
        private set

    var token: String = token
        private set

    var platform: DevicePlatform = platform
        private set

    var active: Boolean = active
        private set

    var lastSeenAt: LocalDateTime = lastSeenAt
        private set

    var invalidatedAt: LocalDateTime? = invalidatedAt
        private set

    var updatedAt: LocalDateTime = updatedAt
        private set

    fun refresh(
        memberId: String,
        installationId: String,
        token: String,
        platform: DevicePlatform,
        now: LocalDateTime = LocalDateTime.now(),
    ) {
        validate(memberId, installationId, token)
        this.memberId = memberId
        this.installationId = installationId
        this.token = token
        this.platform = platform
        active = true
        lastSeenAt = now
        invalidatedAt = null
        updatedAt = now
    }

    fun deactivate(now: LocalDateTime = LocalDateTime.now()) {
        if (!active) {
            return
        }

        active = false
        invalidatedAt = now
        updatedAt = now
    }

    fun canReceivePush(): Boolean = active

    companion object {
        fun register(
            memberId: String,
            installationId: String,
            token: String,
            platform: DevicePlatform,
        ): DevicePushToken {
            validate(memberId, installationId, token)
            val now = LocalDateTime.now()
            return DevicePushToken(
                id = null,
                memberId = memberId,
                installationId = installationId,
                token = token,
                platform = platform,
                active = true,
                lastSeenAt = now,
                invalidatedAt = null,
                createdAt = now,
                updatedAt = now,
            )
        }

        fun reconstitute(
            id: DevicePushTokenId,
            memberId: String,
            installationId: String,
            token: String,
            platform: DevicePlatform,
            active: Boolean,
            lastSeenAt: LocalDateTime,
            invalidatedAt: LocalDateTime?,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,
        ): DevicePushToken {
            validate(memberId, installationId, token)
            return DevicePushToken(
                id = id,
                memberId = memberId,
                installationId = installationId,
                token = token,
                platform = platform,
                active = active,
                lastSeenAt = lastSeenAt,
                invalidatedAt = invalidatedAt,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }

        private fun validate(memberId: String, installationId: String, token: String) {
            require(memberId.isNotBlank()) { "Member id cannot be blank" }
            require(installationId.isNotBlank()) { "Installation id cannot be blank" }
            require(token.isNotBlank()) { "Push token cannot be blank" }
        }
    }
}

package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.NotificationInboxItem
import com.unicorn.server.domain.notification.exception.NotificationNotFoundException
import com.unicorn.server.domain.notification.port.`in`.NotificationInPort
import com.unicorn.server.domain.notification.port.`in`.NotificationMaintenanceInPort
import com.unicorn.server.domain.notification.port.dto.NotificationInboxPage
import com.unicorn.server.domain.notification.port.out.NotificationInboxOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class NotificationService(
    private val notificationInboxOutPort: NotificationInboxOutPort,
) : NotificationInPort,
    NotificationMaintenanceInPort {

    override fun getNotifications(memberId: String, cursor: Long?, size: Int): NotificationInboxPage {
        require(memberId.isNotBlank()) { "Member id cannot be blank" }
        val normalizedSize = size.coerceIn(MIN_PAGE_SIZE, MAX_PAGE_SIZE)
        val items = notificationInboxOutPort.findPageByReceiver(memberId, cursor, normalizedSize + 1)
        val hasNext = items.size > normalizedSize
        val pageItems = items.take(normalizedSize)

        return NotificationInboxPage(
            items = pageItems,
            nextCursor = if (hasNext) pageItems.lastOrNull()?.id?.value else null,
        )
    }

    @Transactional
    override fun markNotificationAsRead(memberId: String, notificationId: Long): NotificationInboxItem {
        require(memberId.isNotBlank()) { "Member id cannot be blank" }
        val item = notificationInboxOutPort.findByIdAndReceiver(notificationId, memberId)
            ?: throw NotificationNotFoundException(notificationId)

        item.markRead()
        return notificationInboxOutPort.save(item)
    }

    @Transactional
    override fun deleteExpiredNotifications(retentionDays: Long): Int {
        require(retentionDays > 0) { "Retention days must be positive" }
        val threshold = LocalDateTime.now().minusDays(retentionDays)
        return notificationInboxOutPort.deleteCreatedBefore(threshold)
    }

    companion object {
        private const val MIN_PAGE_SIZE = 1
        private const val MAX_PAGE_SIZE = 100
    }
}

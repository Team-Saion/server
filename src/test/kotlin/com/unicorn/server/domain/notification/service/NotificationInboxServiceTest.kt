package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.NotificationInboxItem
import com.unicorn.server.domain.notification.NotificationRoute
import com.unicorn.server.domain.notification.enums.NotificationRouteType
import com.unicorn.server.domain.notification.enums.NotificationType
import com.unicorn.server.domain.notification.exception.NotificationNotFoundException
import com.unicorn.server.domain.notification.port.out.NotificationInboxOutPort
import com.unicorn.server.domain.notification.vo.NotificationInboxItemId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("NotificationInboxService 단위 테스트")
class NotificationInboxServiceTest {
	private val notificationInboxOutPort = FakeNotificationInboxOutPort()
	private val notificationInboxService = NotificationInboxService(notificationInboxOutPort)

	@Test
	@DisplayName("알림 보관함 조회 시 현재 멤버의 알림만 최신순으로 반환한다")
	fun getInbox_onlyCurrentMemberNotifications_returnsLatestFirst() {
		val oldItem = notificationInboxOutPort.save(createItem(receiverMemberId = "member-1", dedupKey = "dedup-1"))
		notificationInboxOutPort.save(createItem(receiverMemberId = "member-2", dedupKey = "dedup-2"))
		val newItem = notificationInboxOutPort.save(createItem(receiverMemberId = "member-1", dedupKey = "dedup-3"))

		val page = notificationInboxService.getInbox("member-1", cursor = null, size = 20)

		assertThat(page.items.map { it.id }).containsExactly(newItem.id, oldItem.id)
		assertThat(page.items).allMatch { it.receiverMemberId == "member-1" }
		assertThat(page.nextCursor).isNull()
	}

	@Test
	@DisplayName("조회 크기보다 알림이 많으면 다음 커서를 반환한다")
	fun getInbox_withMoreItems_returnsNextCursor() {
		val oldestItem = notificationInboxOutPort.save(createItem(receiverMemberId = "member-1", dedupKey = "dedup-1"))
		val middleItem = notificationInboxOutPort.save(createItem(receiverMemberId = "member-1", dedupKey = "dedup-2"))
		val newestItem = notificationInboxOutPort.save(createItem(receiverMemberId = "member-1", dedupKey = "dedup-3"))

		val page = notificationInboxService.getInbox("member-1", cursor = null, size = 2)

		assertThat(page.items.map { it.id }).containsExactly(newestItem.id, middleItem.id)
		assertThat(page.nextCursor).isEqualTo(middleItem.id?.value)
		assertThat(page.nextCursor).isNotEqualTo(oldestItem.id?.value)
	}

	@Test
	@DisplayName("보관함 항목 읽음 처리 시 readAt이 저장된다")
	fun markRead_existingNotification_setsReadAt() {
		val item = notificationInboxOutPort.save(createItem(receiverMemberId = "member-1", dedupKey = "dedup-1"))

		val result = notificationInboxService.markRead("member-1", requireNotNull(item.id).value)

		assertThat(result.readAt).isNotNull()
	}

	@Test
	@DisplayName("다른 멤버의 알림 읽음 처리 시 예외가 발생한다")
	fun markRead_otherMemberNotification_throwsException() {
		val item = notificationInboxOutPort.save(createItem(receiverMemberId = "member-1", dedupKey = "dedup-1"))

		assertThatThrownBy { notificationInboxService.markRead("member-2", requireNotNull(item.id).value) }
			.isInstanceOf(NotificationNotFoundException::class.java)
	}

	private fun createItem(receiverMemberId: String, dedupKey: String): NotificationInboxItem =
		NotificationInboxItem.create(
			receiverMemberId = receiverMemberId,
			type = NotificationType.SCHEDULE_CREATED,
			title = "새 일정이 등록됐어요",
			body = "민수님이 '병원 방문' 일정을 추가했어요.",
			route = NotificationRoute.create(
				type = NotificationRouteType.SCHEDULE_DETAIL,
				circleId = "circle-id",
				scheduleId = "schedule-id",
			),
			eventId = dedupKey,
			dedupKey = dedupKey,
		)

	private class FakeNotificationInboxOutPort : NotificationInboxOutPort {
		private val items = linkedMapOf<Long, NotificationInboxItem>()
		private var sequence = 0L

		override fun save(item: NotificationInboxItem): NotificationInboxItem {
			val saved = if (item.id == null) {
				val id = ++sequence
				NotificationInboxItem.reconstitute(
					id = NotificationInboxItemId.of(id),
					receiverMemberId = item.receiverMemberId,
					type = item.type,
					title = item.title,
					body = item.body,
					route = item.route,
					eventId = item.eventId,
					dedupKey = item.dedupKey,
					readAt = item.readAt,
					createdAt = LocalDateTime.now().plusSeconds(id),
					updatedAt = item.updatedAt,
				)
			} else {
				item
			}

			items[requireNotNull(saved.id).value] = saved
			return saved
		}

		override fun findPageByReceiver(memberId: String, cursor: Long?, limit: Int): List<NotificationInboxItem> =
			items.values
				.filter { it.receiverMemberId == memberId }
				.filter { cursor == null || requireNotNull(it.id).value < cursor }
				.sortedByDescending { requireNotNull(it.id).value }
				.take(limit)

		override fun findByIdAndReceiver(notificationId: Long, memberId: String): NotificationInboxItem? =
			items[notificationId]?.takeIf { it.receiverMemberId == memberId }

		override fun deleteCreatedBefore(threshold: LocalDateTime): Int {
			val idsToDelete = items.filterValues { it.createdAt.isBefore(threshold) }.keys
			idsToDelete.forEach(items::remove)
			return idsToDelete.size
		}
	}
}

package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.DevicePushToken
import com.unicorn.server.domain.notification.enums.DevicePlatform
import com.unicorn.server.domain.notification.exception.NotificationNotFoundException
import com.unicorn.server.domain.notification.port.dto.RegisterPushTokenCommand
import com.unicorn.server.domain.notification.port.out.NotificationPushTokenOutPort
import com.unicorn.server.domain.notification.vo.DevicePushTokenId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("NotificationPushTokenService 단위 테스트")
class NotificationPushTokenServiceTest {
	private val notificationPushTokenOutPort = FakeNotificationPushTokenOutPort()
	private val notificationPushTokenService = NotificationPushTokenService(notificationPushTokenOutPort)

	@Test
	@DisplayName("새 푸시 토큰을 등록한다")
	fun register_newToken_savesToken() {
		val command = RegisterPushTokenCommand(
			token = "token-1",
			platform = DevicePlatform.IOS,
			osNotificationPermissionGranted = true,
			appVersion = "1.0.0",
		)

		val pushToken = notificationPushTokenService.register("member-1", command)

		assertThat(pushToken.id).isNotNull()
		assertThat(pushToken.memberId).isEqualTo("member-1")
		assertThat(pushToken.canReceivePush()).isTrue()
	}

	@Test
	@DisplayName("기존 푸시 토큰을 다시 등록하면 최신 권한 상태로 갱신한다")
	fun register_existingToken_refreshesToken() {
		notificationPushTokenService.register(
			"member-1",
			RegisterPushTokenCommand("token-1", DevicePlatform.IOS, true, "1.0.0"),
		)

		val refreshed = notificationPushTokenService.register(
			"member-1",
			RegisterPushTokenCommand("token-1", DevicePlatform.IOS, false, "1.0.1"),
		)

		assertThat(notificationPushTokenOutPort.tokens).hasSize(1)
		assertThat(refreshed.osNotificationPermissionGranted).isFalse()
		assertThat(refreshed.appVersion).isEqualTo("1.0.1")
		assertThat(refreshed.active).isTrue()
	}

	@Test
	@DisplayName("푸시 토큰 비활성화 시 발송 대상에서 제외된다")
	fun deactivate_existingToken_deactivatesToken() {
		val pushToken = notificationPushTokenService.register(
			"member-1",
			RegisterPushTokenCommand("token-1", DevicePlatform.ANDROID, true, "1.0.0"),
		)

		notificationPushTokenService.deactivate("member-1", requireNotNull(pushToken.id).value)

		val saved = notificationPushTokenOutPort.findByToken("token-1")
		assertThat(saved?.active).isFalse()
		assertThat(saved?.canReceivePush()).isFalse()
	}

	@Test
	@DisplayName("다른 멤버의 푸시 토큰 비활성화 시 예외가 발생한다")
	fun deactivate_otherMemberToken_throwsException() {
		val pushToken = notificationPushTokenService.register(
			"member-1",
			RegisterPushTokenCommand("token-1", DevicePlatform.ANDROID, true, "1.0.0"),
		)

		assertThatThrownBy { notificationPushTokenService.deactivate("member-2", requireNotNull(pushToken.id).value) }
			.isInstanceOf(NotificationNotFoundException::class.java)
	}

	private class FakeNotificationPushTokenOutPort : NotificationPushTokenOutPort {
		val tokens = linkedMapOf<Long, DevicePushToken>()
		private var sequence = 0L

		override fun save(pushToken: DevicePushToken): DevicePushToken {
			val saved = if (pushToken.id == null) {
				val id = ++sequence
				DevicePushToken.reconstitute(
					id = DevicePushTokenId.of(id),
					memberId = pushToken.memberId,
					token = pushToken.token,
					platform = pushToken.platform,
					osNotificationPermissionGranted = pushToken.osNotificationPermissionGranted,
					appVersion = pushToken.appVersion,
					active = pushToken.active,
					lastSeenAt = pushToken.lastSeenAt,
					invalidatedAt = pushToken.invalidatedAt,
					createdAt = pushToken.createdAt,
					updatedAt = LocalDateTime.now().plusSeconds(id),
				)
			} else {
				pushToken
			}

			tokens[requireNotNull(saved.id).value] = saved
			return saved
		}

		override fun findByToken(token: String): DevicePushToken? = tokens.values.firstOrNull { it.token == token }

		override fun findByIdAndMemberId(tokenId: Long, memberId: String): DevicePushToken? =
			tokens[tokenId]?.takeIf { it.memberId == memberId }

		override fun findActiveReceivableByMemberId(memberId: String): List<DevicePushToken> =
			tokens.values.filter { it.memberId == memberId && it.canReceivePush() }
	}
}

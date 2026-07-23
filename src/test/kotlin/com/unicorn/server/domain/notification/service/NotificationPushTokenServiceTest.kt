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
	@DisplayName("새 푸시 토큰을 설치 식별자와 함께 등록한다")
	fun register_newToken_savesToken() {
		val command = RegisterPushTokenCommand(
			installationId = "installation-1",
			token = "token-1",
			platform = DevicePlatform.IOS,
		)

		val pushToken = notificationPushTokenService.register("member-1", command)

		assertThat(pushToken.id).isNotNull()
		assertThat(pushToken.memberId).isEqualTo("member-1")
		assertThat(pushToken.installationId).isEqualTo("installation-1")
		assertThat(pushToken.canReceivePush()).isTrue()
	}

	@Test
	@DisplayName("같은 설치의 FCM 토큰이 변경되면 기존 행을 갱신한다")
	fun register_existingInstallationWithNewToken_updatesToken() {
		notificationPushTokenService.register(
			"member-1",
			RegisterPushTokenCommand("installation-1", "token-1", DevicePlatform.IOS),
		)

		val refreshed = notificationPushTokenService.register(
			"member-1",
			RegisterPushTokenCommand("installation-1", "token-2", DevicePlatform.ANDROID),
		)

		assertThat(notificationPushTokenOutPort.tokens).hasSize(1)
		assertThat(refreshed.token).isEqualTo("token-2")
		assertThat(refreshed.platform).isEqualTo(DevicePlatform.ANDROID)
		assertThat(refreshed.active).isTrue()
	}

	@Test
	@DisplayName("기존 토큰이 실제 설치 식별자로 처음 등록되면 기존 행을 갱신한다")
	fun register_existingTokenWithNewInstallationId_updatesInstallationId() {
		notificationPushTokenService.register(
			"member-1",
			RegisterPushTokenCommand("legacy-1", "token-1", DevicePlatform.IOS),
		)

		val refreshed = notificationPushTokenService.register(
			"member-1",
			RegisterPushTokenCommand("installation-1", "token-1", DevicePlatform.IOS),
		)

		assertThat(notificationPushTokenOutPort.tokens).hasSize(1)
		assertThat(refreshed.installationId).isEqualTo("installation-1")
	}

	@Test
	@DisplayName("푸시 토큰을 비활성화하면 발송 대상에서 제외한다")
	fun deactivate_existingToken_deactivatesToken() {
		val pushToken = notificationPushTokenService.register(
			"member-1",
			RegisterPushTokenCommand("installation-1", "token-1", DevicePlatform.ANDROID),
		)

		notificationPushTokenService.deactivate("member-1", requireNotNull(pushToken.id).value)

		val saved = notificationPushTokenOutPort.findByToken("token-1")
		assertThat(saved?.active).isFalse()
		assertThat(saved?.canReceivePush()).isFalse()
	}

	@Test
	@DisplayName("다른 멤버의 푸시 토큰을 비활성화하면 예외가 발생한다")
	fun deactivate_otherMemberToken_throwsException() {
		val pushToken = notificationPushTokenService.register(
			"member-1",
			RegisterPushTokenCommand("installation-1", "token-1", DevicePlatform.ANDROID),
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
					installationId = pushToken.installationId,
					token = pushToken.token,
					platform = pushToken.platform,
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

		override fun findByInstallationId(installationId: String): DevicePushToken? =
			tokens.values.firstOrNull { it.installationId == installationId }

		override fun findByToken(token: String): DevicePushToken? =
			tokens.values.firstOrNull { it.token == token }

		override fun findByIdAndMemberId(tokenId: Long, memberId: String): DevicePushToken? =
			tokens[tokenId]?.takeIf { it.memberId == memberId }

		override fun findActiveReceivableByMemberId(memberId: String): List<DevicePushToken> =
			tokens.values.filter { it.memberId == memberId && it.canReceivePush() }
	}
}

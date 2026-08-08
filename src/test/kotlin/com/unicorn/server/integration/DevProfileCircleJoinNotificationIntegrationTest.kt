package com.unicorn.server.integration

import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.circle.port.`in`.CircleInPort
import com.unicorn.server.domain.circle.port.dto.CreateCircleCommand
import com.unicorn.server.domain.invitation.port.`in`.InvitationAcceptInPort
import com.unicorn.server.domain.invitation.port.`in`.InvitationIssueInPort
import com.unicorn.server.domain.invitation.port.dto.IssueInvitationCommand
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.notification.enums.DevicePlatform
import com.unicorn.server.domain.notification.enums.NotificationStatus
import com.unicorn.server.domain.notification.enums.NotificationType
import com.unicorn.server.domain.notification.port.`in`.NotificationPushTokenInPort
import com.unicorn.server.domain.notification.port.dto.RegisterPushTokenCommand
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.NotificationInboxJpaRepository
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.NotificationJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
	properties = [
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.url=jdbc:h2:mem:notification-dev-profile-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.flyway.enabled=true",
		"app.notification.dispatch.enabled=false",
		"app.notification.fcm.enabled=false",
		"app.jwt.secret=dev-profile-notification-test-secret-key",
		"app.kakao.app-key=dev-profile-test-kakao-key",
		"app.apple.client-id=dev-profile-test-apple-client-id",
		"app.s3.bucket=dev-profile-test-bucket",
	],
)
@ActiveProfiles("dev")
@DisplayName("개발 프로필 써클 참여 알림 통합 테스트")
class DevProfileCircleJoinNotificationIntegrationTest(
	@param:Autowired private val memberOutPort: MemberOutPort,
	@param:Autowired private val circleInPort: CircleInPort,
	@param:Autowired private val invitationIssueInPort: InvitationIssueInPort,
	@param:Autowired private val invitationAcceptInPort: InvitationAcceptInPort,
	@param:Autowired private val pushTokenInPort: NotificationPushTokenInPort,
	@param:Autowired private val notificationJpaRepository: NotificationJpaRepository,
	@param:Autowired private val notificationInboxJpaRepository: NotificationInboxJpaRepository,
) {
	@Test
	@DisplayName("초대 수락 시 notification과 notification_inbox_item에 각각 저장된다")
	fun acceptInvitation_persistsNotificationAndInboxItem() {
		val owner = memberOutPort.save(
			Member.create(Email("dev-owner@test.local"), "DevOwner", "devOwner", role = Role.MEMBER),
		)
		val invitee = memberOutPort.save(
			Member.create(Email("dev-invitee@test.local"), "DevInvitee", "devInvitee", role = Role.MEMBER),
		)
		val pushToken = pushTokenInPort.register(
			owner.id.toString(),
			RegisterPushTokenCommand(
				installationId = "dev-profile-circle-join-installation",
				token = "dev-profile-circle-join-token",
				platform = DevicePlatform.ANDROID,
			),
		)
		val circle = circleInPort.create(owner.id.toString(), CreateCircleCommand("개발환경알림"))
		val invitation = invitationIssueInPort.issue(
			owner.id.toString(),
			IssueInvitationCommand(circle.id),
		)

		invitationAcceptInPort.accept(invitation.token, invitee.id.toString())

		val receiverDedupKey = "circle_join_completed:${invitation.invitationId}:${owner.id}"
		val pushDedupKey = "$receiverDedupKey:token:${requireNotNull(pushToken.id).value}"
		val inboxItem = notificationInboxJpaRepository.findByDedupKey(receiverDedupKey)
		val pushNotification = notificationJpaRepository.findByDedupKey(pushDedupKey)

		assertThat(inboxItem?.type).isEqualTo(NotificationType.CIRCLE_JOIN_COMPLETED)
		assertThat(inboxItem?.receiverMemberId).isEqualTo(owner.id.toString())
		assertThat(pushNotification?.type).isEqualTo(NotificationType.CIRCLE_JOIN_COMPLETED)
		assertThat(pushNotification?.receiver).isEqualTo("dev-profile-circle-join-token")
		assertThat(pushNotification?.status).isEqualTo(NotificationStatus.READY)
	}
}

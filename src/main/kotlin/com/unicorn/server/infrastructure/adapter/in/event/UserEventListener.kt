package com.unicorn.server.infrastructure.adapter.`in`.event

import com.unicorn.server.domain.user.event.UserSignedUpEvent
import com.unicorn.server.domain.user.event.UserWithdrawnEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class UserEventListener {
	@EventListener
	fun handleUserSignedUp(event: UserSignedUpEvent) {
		log.info("User signed up - userId: {}, email: {}", event.userId, event.email)
		// @TODO 이메일 발송: 회원가입 환영 이메일 발송 (EmailNotificationService.sendWelcomeEmail)
		// @TODO 포인트 지급: 신규 회원 가입 축하 포인트 지급 (PointService.grantSignupBonus)
	}

	@EventListener
	fun handleUserWithdrawn(event: UserWithdrawnEvent) {
		log.info("User withdrawn - userId: {}", event.userId)
		// @TODO 개인정보 처리: 30일 후 개인정보 삭제 처리 (PrivacyService.anonymizeUserData)
		// @TODO 이메일 발송: 탈퇴 완료 알림 이메일 발송 (EmailNotificationService.sendWithdrawalConfirmation)
	}

	companion object {
		private val log = LoggerFactory.getLogger(UserEventListener::class.java)
	}
}

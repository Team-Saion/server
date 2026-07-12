package com.unicorn.server.infrastructure.adapter.`in`.event

import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.member.event.MemberWithdrawnEvent
import com.unicorn.server.domain.member.port.out.TokenStore
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

// MemberEventListener - 멤버 도메인 이벤트의 인프라 후속 처리를 담당한다.
@Component
class MemberEventListener(
	private val tokenStore: TokenStore,
	private val circleMemberInPort: CircleMemberInPort,
) {
	// 멤버 탈퇴 이벤트를 수신해 후속 정리 작업의 시작점을 제공한다.
	@EventListener
	fun handleMemberWithdrawn(event: MemberWithdrawnEvent) {
		log.info("Member withdrawn - memberId: {}", event.memberId)
		tokenStore.deleteByMemberId(event.memberId)
		runCatching { circleMemberInPort.handleMemberWithdrawal(event.memberId) }
			.onFailure { e -> log.error("Failed to handle circle member withdrawal - memberId: {}", event.memberId, e) }
	}

	companion object {
		private val log = LoggerFactory.getLogger(MemberEventListener::class.java)
	}
}

package com.unicorn.server.infrastructure.adapter.`in`.event

import com.unicorn.server.domain.member.event.MemberWithdrawnEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

// MemberEventListener - 멤버 도메인 이벤트의 인프라 후속 처리를 담당한다.
@Component
class MemberEventListener {
	// 멤버 탈퇴 이벤트를 수신해 후속 정리 작업의 시작점을 제공한다.
	@EventListener
	fun handleMemberWithdrawn(event: MemberWithdrawnEvent) {
		log.info("Member withdrawn - memberId: {}", event.memberId)
		// TODO: 탈퇴 후 정리 작업 (개인정보 익명화 스케줄러 등록 등)
	}

	companion object {
		private val log = LoggerFactory.getLogger(MemberEventListener::class.java)
	}
}

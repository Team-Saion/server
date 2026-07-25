package com.unicorn.server.domain.member.port.`in`

// MemberWithdrawInPort - 회원탈퇴 유스케이스 진입점을 정의한다.
interface MemberWithdrawInPort {
	// 멤버를 soft delete 상태로 전환한다.
	fun withdraw(memberId: String, reason: String)
}

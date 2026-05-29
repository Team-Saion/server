package com.unicorn.server.domain.member.port.out

import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.vo.MemberId
import java.time.LocalDateTime

// MemberOutPort - 멤버 도메인이 필요로 하는 저장소 기능을 정의한다.
interface MemberOutPort {
	// 멤버 상태를 저장하고 저장된 도메인 객체를 반환한다.
	fun save(member: Member): Member

	// 멤버 식별자로 멤버를 조회한다.
	fun findById(memberId: MemberId): Member?

	// 이메일로 멤버를 조회한다.
	fun findByEmail(email: Email): Member?

	// 이메일 중복 여부를 확인한다.
	fun existsByEmail(email: Email): Boolean

	// 보관 기간이 지난 탈퇴 멤버 목록을 조회한다.
	fun findAllDeletedBefore(threshold: LocalDateTime): List<Member>
}

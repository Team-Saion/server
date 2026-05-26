package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.domain.member.enums.Role
import java.time.LocalDateTime

// MemberResponse - 멤버 조회/변경 결과를 외부로 전달하는 응답 DTO다.
data class MemberResponse(
    val id: String,
    val email: String,
    val name: String,
    val nickname: String,
    val role: Role,
    val profileImageKey: String?,
    val status: MemberStatus,
    val createdAt: LocalDateTime,
) {
	companion object {
		// 도메인 멤버를 응답 DTO로 변환한다.
		fun from(member: Member): MemberResponse = MemberResponse(
			id = member.id.toString(),
			email = member.email.value,
			name = member.name,
			nickname = member.nickname,
			role = member.role,
			profileImageKey = member.profileImageKey,
			status = member.status,
			createdAt = member.createdAt,
		)
	}
}
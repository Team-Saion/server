package com.unicorn.server.domain.member

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.exception.MemberErrorCode
import com.unicorn.server.domain.member.vo.MemberId
import java.time.LocalDateTime

// Member 도메인 - 서비스 관점의 멤버 프로필과 탈퇴 생명주기 규칙을 담당한다.
class Member private constructor(
	val id: MemberId,
	val email: Email,
	val name: String,
	nickname: String,
	val role: Role,
	profileImageKey: String?,
	status: MemberStatus,
	deletedAt: LocalDateTime?,
	val createdAt: LocalDateTime,
	updatedAt: LocalDateTime,
) {
	var nickname: String = nickname
		private set

	var profileImageKey: String? = profileImageKey
		private set

	var status: MemberStatus = status
		private set

	var deletedAt: LocalDateTime? = deletedAt
		private set

	var updatedAt: LocalDateTime = updatedAt
		private set

	// 멤버를 탈퇴 상태로 전환하고 soft delete 시각을 기록한다.
	fun withdraw() {
		if (status == MemberStatus.DELETED) {
			throw BusinessException(MemberErrorCode.MEMBER_ALREADY_DELETED)
		}

		status = MemberStatus.DELETED
		deletedAt = LocalDateTime.now()
		updatedAt = LocalDateTime.now()
	}

	// 멤버 프로필의 닉네임을 검증 후 변경한다.
	fun updateProfile(nickname: String) {
		validateNickname(nickname)

		this.nickname = nickname
		updatedAt = LocalDateTime.now()
	}

	// 멤버가 탈퇴 상태인지 확인한다.
	fun isDeleted(): Boolean = status == MemberStatus.DELETED

	companion object {
		const val WITHDRAWAL_RETENTION_DAYS = 30L

		// 신규 멤버를 기본 역할과 활성 상태로 생성한다.
		fun create(
			email: Email,
			name: String,
			nickname: String,
			role: Role = Role.MEMBER,
		): Member {
			validateName(name)
			validateNickname(nickname)

			val now = LocalDateTime.now()
			return Member(
				id = MemberId.generate(),
				email = email,
				name = name,
				nickname = nickname,
				role = role,
				profileImageKey = null,
				status = MemberStatus.ACTIVE,
				deletedAt = null,
				createdAt = now,
				updatedAt = now,
			)
		}

		// 저장소의 원시 상태를 도메인 멤버로 복원한다.
		fun reconstitute(
			id: MemberId,
			email: Email,
			name: String,
			nickname: String,
			role: Role,
			profileImageKey: String?,
			status: MemberStatus,
			deletedAt: LocalDateTime?,
			createdAt: LocalDateTime,
			updatedAt: LocalDateTime,
		): Member = Member(id, email, name, nickname, role, profileImageKey, status, deletedAt, createdAt, updatedAt)

		// 실명 또는 소셜 제공 이름의 최소 유효성을 검증한다.
		private fun validateName(name: String) {
			require(name.isNotBlank()) { "Name cannot be blank" }
		}

		// 서비스 내 노출 닉네임의 최소/최대 길이를 검증한다.
		private fun validateNickname(nickname: String) {
			require(nickname.isNotBlank()) { "Nickname cannot be blank" }
			require(nickname == nickname.trim()) { "Nickname must not have leading or trailing whitespace" }
			require(nickname.length in 2..30) { "Nickname must be between 2 and 30 characters" }
		}
	}
}

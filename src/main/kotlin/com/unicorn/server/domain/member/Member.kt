package com.unicorn.server.domain.member

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.enums.AvatarColor
import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.exception.MemberErrorCode
import com.unicorn.server.domain.member.vo.MemberId
import java.time.LocalDateTime

// Member 도메인 - 서비스 관점의 멤버 프로필과 탈퇴 생명주기 규칙을 담당한다.
class Member internal constructor(
	val id: MemberId,
	val email: Email?,
	val name: String?,
	nickname: String,
	val avatarColor: AvatarColor,
	role: Role,
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

	var role: Role = role
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

	// 온보딩을 완료하고 정식 멤버 역할로 전환한다.
	fun completeOnboarding(nickname: String) {
		check(this.role == Role.PENDING) { "Member is already onboarded" }
		val trimmed = nickname.trim()
		validateNickname(trimmed)
		this.nickname = trimmed
		this.role = Role.MEMBER
		this.updatedAt = LocalDateTime.now()
	}

	// 프로필 이미지 키를 교체한다. 업로드 검증/스토리지 연동은 use-case 책임이다.
	fun changeProfileImage(objectKey: String) {
		require(objectKey.isNotBlank()) { "Profile image key cannot be blank" }

		profileImageKey = objectKey
		updatedAt = LocalDateTime.now()
	}

	// 멤버가 탈퇴 상태인지 확인한다.
	fun isDeleted(): Boolean = status == MemberStatus.DELETED

	companion object {
		const val WITHDRAWAL_RETENTION_DAYS = 30L
		private const val MIN_NICKNAME_LENGTH = 2
		private const val MAX_NICKNAME_LENGTH = 10
		private val NICKNAME_PATTERN = Regex("^[가-힣a-zA-Z0-9]+$")

		// 신규 멤버를 기본 역할과 활성 상태로 생성한다.
		fun create(
			id: MemberId,
			email: Email?,
			name: String?,
			nickname: String,
			role: Role = Role.MEMBER,
		): Member {
			validateName(name)
			validateNickname(nickname)

			val now = LocalDateTime.now()
			return Member(
				id = id,
				email = email,
				name = name,
				nickname = nickname,
				avatarColor = AvatarColor.random(),
				role = role,
				profileImageKey = null,
				status = MemberStatus.ACTIVE,
				deletedAt = null,
				createdAt = now,
				updatedAt = now,
			)
		}

		// 실명 또는 소셜 제공 이름의 최소 유효성을 검증한다.
		private fun validateName(name: String?) {
			if (name != null) require(name.isNotBlank()) { "Name cannot be blank" }
		}

		// 서비스 내 노출 닉네임의 길이와 허용 문자를 검증한다.
		private fun validateNickname(nickname: String) {
			if (nickname.isBlank() ||
				nickname != nickname.trim() ||
				nickname.length !in MIN_NICKNAME_LENGTH..MAX_NICKNAME_LENGTH ||
				!NICKNAME_PATTERN.matches(nickname)
			) {
				throw BusinessException(MemberErrorCode.INVALID_NICKNAME)
			}
		}
	}
}

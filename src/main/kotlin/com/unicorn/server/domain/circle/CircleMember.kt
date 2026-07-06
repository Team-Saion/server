package com.unicorn.server.domain.circle

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.circle.enums.CircleMemberStatus
import com.unicorn.server.domain.circle.enums.CircleRole
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.circle.vo.CircleMemberId
import com.unicorn.server.domain.member.vo.MemberId
import java.time.LocalDateTime

class CircleMember internal constructor(
	val id: CircleMemberId,
	val circleId: CircleId,
	val memberId: MemberId,
	nickname: String,
	val role: CircleRole,
	status: CircleMemberStatus,
	val joinedAt: LocalDateTime,
	leftAt: LocalDateTime?,
	deleted: Boolean,
	val createdAt: LocalDateTime,
	updatedAt: LocalDateTime,
) {
	var nickname: String = nickname
		private set

	var status: CircleMemberStatus = status
		private set

	var leftAt: LocalDateTime? = leftAt
		private set

	var deleted: Boolean = deleted
		private set

	var updatedAt: LocalDateTime = updatedAt
		private set

	fun leave() {
		if (role == CircleRole.INITIATOR) {
			throw BusinessException(CircleErrorCode.INITIATOR_CANNOT_LEAVE)
		}
		status = CircleMemberStatus.LEFT
		leftAt = LocalDateTime.now()
		updatedAt = LocalDateTime.now()
	}

	fun rejoin(newNickname: String) {
		if (status != CircleMemberStatus.LEFT) {
			throw BusinessException(CircleErrorCode.ALREADY_JOINED)
		}
		status = CircleMemberStatus.ACTIVE
		nickname = validateNickname(newNickname)
		leftAt = null
		updatedAt = LocalDateTime.now()
	}

	companion object {
		fun createInitiator(id: CircleMemberId, circleId: CircleId, memberId: MemberId, nickname: String): CircleMember {
			val now = LocalDateTime.now()
			return CircleMember(
				id = id,
				circleId = circleId,
				memberId = memberId,
				nickname = validateNickname(nickname),
				role = CircleRole.INITIATOR,
				status = CircleMemberStatus.ACTIVE,
				joinedAt = now,
				leftAt = null,
				deleted = false,
				createdAt = now,
				updatedAt = now,
			)
		}

		fun createMember(id: CircleMemberId, circleId: CircleId, memberId: MemberId, nickname: String): CircleMember {
			val now = LocalDateTime.now()
			return CircleMember(
				id = id,
				circleId = circleId,
				memberId = memberId,
				nickname = validateNickname(nickname),
				role = CircleRole.MEMBER,
				status = CircleMemberStatus.ACTIVE,
				joinedAt = now,
				leftAt = null,
				deleted = false,
				createdAt = now,
				updatedAt = now,
			)
		}

		private fun validateNickname(nickname: String): String {
			val trimmed = nickname.trim()
			if (trimmed.isBlank() || trimmed.length > MAX_LENGTH) {
				throw BusinessException(CircleErrorCode.CIRCLE_NICKNAME_INVALID)
			}
			return trimmed
		}

		private const val MAX_LENGTH = 30
	}
}

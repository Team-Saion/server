package com.unicorn.server.domain.circle

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.member.vo.MemberId
import java.time.LocalDateTime

class Circle internal constructor(
	val id: CircleId,
	name: String,
	ownerId: MemberId,
	deleted: Boolean,
	val createdAt: LocalDateTime,
	updatedAt: LocalDateTime,
) {
	var name: String = name
		private set

	var ownerId: MemberId = ownerId
		private set

	var deleted: Boolean = deleted
		private set

	var updatedAt: LocalDateTime = updatedAt
		private set

	fun rename(newName: String) {
		name = validateName(newName)
		updatedAt = LocalDateTime.now()
	}

	fun softDelete() {
		deleted = true
		updatedAt = LocalDateTime.now()
	}

	fun transferOwner(newOwnerId: MemberId) {
		ownerId = newOwnerId
		updatedAt = LocalDateTime.now()
	}

	companion object {
		fun create(id: CircleId, name: String, ownerId: MemberId): Circle {
			val now = LocalDateTime.now()
			return Circle(id, validateName(name), ownerId, false, now, now)
		}

		private fun validateName(name: String): String {
			val trimmed = name.trim()
			if (trimmed.isBlank()) {
				throw BusinessException(CircleErrorCode.CIRCLE_NAME_BLANK)
			}
			if (trimmed.codePointCount(0, trimmed.length) > MAX_LENGTH) {
				throw BusinessException(CircleErrorCode.CIRCLE_NAME_TOO_LONG)
			}
			if (FORBIDDEN_CHARACTER_PATTERN.containsMatchIn(trimmed)) {
				throw BusinessException(CircleErrorCode.CIRCLE_NAME_INVALID_CHARSET)
			}
			return trimmed
		}

		private const val MAX_LENGTH = 20
		private val FORBIDDEN_CHARACTER_PATTERN = Regex("[<>&\"'\\\\]|\\p{Cntrl}")
	}
}

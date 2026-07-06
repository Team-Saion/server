package com.unicorn.server.domain.circle

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.member.vo.MemberId
import java.time.LocalDateTime

class Circle internal constructor(
	val id: CircleId,
	name: String,
	val ownerId: MemberId,
	deleted: Boolean,
	val createdAt: LocalDateTime,
	updatedAt: LocalDateTime,
) {
	var name: String = name
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
			if (trimmed.length > MAX_LENGTH) {
				throw BusinessException(CircleErrorCode.CIRCLE_NAME_TOO_LONG)
			}
			if (!CHARSET_PATTERN.matches(trimmed)) {
				throw BusinessException(CircleErrorCode.CIRCLE_NAME_INVALID_CHARSET)
			}
			return trimmed
		}

		private const val MAX_LENGTH = 20
		private val CHARSET_PATTERN = Regex("^[가-힣a-zA-Z0-9]+$")
	}
}

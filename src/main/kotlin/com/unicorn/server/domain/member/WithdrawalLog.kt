package com.unicorn.server.domain.member

import com.unicorn.server.domain.member.vo.MemberId
import java.time.LocalDateTime

class WithdrawalLog private constructor(
	val memberId: MemberId,
	val originalEmail: String?,
	val reason: String,
	val withdrawnAt: LocalDateTime,
) {
	companion object {
		fun create(
			memberId: MemberId,
			originalEmail: String?,
			reason: String,
			withdrawnAt: LocalDateTime,
		): WithdrawalLog {
			require(reason.isNotBlank()) { "Reason cannot be blank" }
			require(reason.length <= 500) { "Reason cannot exceed 500 characters" }
			return WithdrawalLog(memberId, originalEmail, reason, withdrawnAt)
		}
	}
}

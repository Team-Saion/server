package com.unicorn.server.domain.term

import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.term.vo.MemberTermId
import com.unicorn.server.domain.term.vo.TermId
import java.time.LocalDateTime

class MemberTerm internal constructor(
	val id: MemberTermId?,
	val memberId: MemberId,
	val termId: TermId,
	val agreedAt: LocalDateTime,
) {
	companion object {
		fun create(memberId: MemberId, termId: TermId): MemberTerm = MemberTerm(
			id = null,
			memberId = memberId,
			termId = termId,
			agreedAt = LocalDateTime.now(),
		)
	}
}

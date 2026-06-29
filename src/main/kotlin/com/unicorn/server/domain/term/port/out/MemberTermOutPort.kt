package com.unicorn.server.domain.term.port.out

import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.term.MemberTerm

interface MemberTermOutPort {
	fun saveAll(memberTerms: List<MemberTerm>): List<MemberTerm>
	fun findAllByMemberId(memberId: MemberId): List<MemberTerm>
}

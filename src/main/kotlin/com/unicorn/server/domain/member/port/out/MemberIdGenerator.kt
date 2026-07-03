package com.unicorn.server.domain.member.port.out

import com.unicorn.server.domain.member.vo.MemberId

interface MemberIdGenerator {
	fun next(): MemberId
}

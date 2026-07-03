package com.unicorn.server.domain.circle.port.out

import com.unicorn.server.domain.circle.vo.CircleMemberId

interface CircleMemberIdGenerator {
	fun next(): CircleMemberId
}

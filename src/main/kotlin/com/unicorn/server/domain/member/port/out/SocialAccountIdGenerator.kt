package com.unicorn.server.domain.member.port.out

import com.unicorn.server.domain.member.vo.SocialAccountId

interface SocialAccountIdGenerator {
	fun next(): SocialAccountId
}

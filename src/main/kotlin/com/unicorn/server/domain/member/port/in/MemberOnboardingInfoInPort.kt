package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.port.dto.OnboardingInfoResult

interface MemberOnboardingInfoInPort {
	fun getOnboardingInfo(memberId: String): OnboardingInfoResult
}

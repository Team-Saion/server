package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.port.dto.OnboardingInfoResult

interface GetOnboardingInfoInPort {
	fun getOnboardingInfo(memberId: String): OnboardingInfoResult
}

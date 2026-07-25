package com.unicorn.server.domain.member.port.`in`

import com.unicorn.server.domain.member.port.dto.CompleteOnboardingCommand
import com.unicorn.server.domain.member.port.dto.TokenPair

interface MemberOnboardingCompleteInPort {
	fun completeOnboarding(memberId: String, command: CompleteOnboardingCommand): TokenPair
}

package com.unicorn.server.domain.member.port.out

import com.unicorn.server.domain.member.WithdrawalLog

interface WithdrawalLogOutPort {
	fun save(log: WithdrawalLog)
}

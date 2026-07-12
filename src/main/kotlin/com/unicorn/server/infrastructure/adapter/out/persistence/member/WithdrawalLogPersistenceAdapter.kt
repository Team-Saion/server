package com.unicorn.server.infrastructure.adapter.out.persistence.member

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.member.WithdrawalLog
import com.unicorn.server.domain.member.port.out.WithdrawalLogOutPort
import com.unicorn.server.infrastructure.adapter.out.persistence.member.entity.WithdrawalLogEntity
import org.springframework.transaction.annotation.Transactional

@PersistenceAdapter
class WithdrawalLogPersistenceAdapter(
	private val withdrawalLogJpaRepository: WithdrawalLogJpaRepository,
) : WithdrawalLogOutPort {

	@Transactional
	override fun save(log: WithdrawalLog) {
		val entity = WithdrawalLogEntity(
			memberId = log.memberId.toString(),
			originalEmail = log.originalEmail,
			reason = log.reason,
			withdrawnAt = log.withdrawnAt,
		)
		withdrawalLogJpaRepository.save(entity)
	}
}

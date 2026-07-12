package com.unicorn.server.infrastructure.adapter.out.persistence.member

import com.unicorn.server.infrastructure.adapter.out.persistence.member.entity.WithdrawalLogEntity
import org.springframework.data.jpa.repository.JpaRepository

interface WithdrawalLogJpaRepository : JpaRepository<WithdrawalLogEntity, Long>

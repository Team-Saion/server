package com.unicorn.server.infrastructure.adapter.out.persistence.invitation

import com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity.InvitationRedemptionLogEntity
import org.springframework.data.jpa.repository.JpaRepository

interface InvitationRedemptionLogJpaRepository : JpaRepository<InvitationRedemptionLogEntity, String>

package com.unicorn.server.infrastructure.adapter.out.persistence.invitation

import com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity.InvitationClickLogEntity
import org.springframework.data.jpa.repository.JpaRepository

interface InvitationClickLogJpaRepository : JpaRepository<InvitationClickLogEntity, String>

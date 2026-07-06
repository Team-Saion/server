package com.unicorn.server.infrastructure.adapter.out.persistence.invitation

import com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity.InvitationDispatchLogEntity
import org.springframework.data.jpa.repository.JpaRepository

interface InvitationDispatchLogJpaRepository : JpaRepository<InvitationDispatchLogEntity, String>

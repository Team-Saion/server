package com.unicorn.server.infrastructure.adapter.out.persistence.term

import com.unicorn.server.infrastructure.adapter.out.persistence.term.entity.MemberTermEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MemberTermJpaRepository : JpaRepository<MemberTermEntity, Long>

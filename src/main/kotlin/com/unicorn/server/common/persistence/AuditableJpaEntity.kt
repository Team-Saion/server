package com.unicorn.server.common.persistence

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AuditableJpaEntity {

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	var createdAt: LocalDateTime? = null
		internal set

	@LastModifiedDate
	@Column(name = "updated_at")
	var updatedAt: LocalDateTime? = null
		internal set

	@CreatedBy
	@Column(name = "created_by", updatable = false, length = 100)
	var createdBy: String? = null
		protected set

	@LastModifiedBy
	@Column(name = "updated_by", length = 100)
	var updatedBy: String? = null
		protected set

}

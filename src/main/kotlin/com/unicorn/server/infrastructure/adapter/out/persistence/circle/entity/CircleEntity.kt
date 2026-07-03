package com.unicorn.server.infrastructure.adapter.out.persistence.circle.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "circle")
class CircleEntity internal constructor() : AuditableJpaEntity() {
	@Id
	@Column(name = "id", nullable = false, length = 21)
	var id: String = ""
		internal set

	@Column(name = "name", nullable = false, length = 20)
	var name: String = ""
		internal set

	@Column(name = "owner_id", nullable = false, length = 36)
	var ownerId: String = ""
		internal set

	@Column(name = "del_yn", nullable = false, length = 1)
	var delYn: String = "N"
		internal set


}

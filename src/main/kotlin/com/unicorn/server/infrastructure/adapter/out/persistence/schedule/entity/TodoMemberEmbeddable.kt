package com.unicorn.server.infrastructure.adapter.out.persistence.schedule.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class TodoMemberEmbeddable(
	@Column(name = "member_id", nullable = false, length = 100)
	val memberId: String = "",
	@Column(name = "checked", nullable = false)
	val checked: Boolean = false,
)

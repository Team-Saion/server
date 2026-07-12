package com.unicorn.server.infrastructure.adapter.out.persistence.member.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
	name = "withdrawal_log",
	indexes = [Index(name = "idx_withdrawal_log_member_id", columnList = "member_id")],
)
class WithdrawalLogEntity protected constructor() {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	var id: Long = 0
		internal set

	@Column(name = "member_id", nullable = false, length = 36)
	var memberId: String = ""
		internal set

	@Column(name = "original_email", nullable = true, length = 255)
	var originalEmail: String? = null
		internal set

	@Column(name = "reason", nullable = false, length = 500)
	var reason: String = ""
		internal set

	@Column(name = "withdrawn_at", nullable = false)
	var withdrawnAt: LocalDateTime = LocalDateTime.MIN
		internal set

	@Column(name = "created_at", nullable = false, updatable = false)
	var createdAt: LocalDateTime = LocalDateTime.now()
		internal set

	constructor(
		memberId: String,
		originalEmail: String?,
		reason: String,
		withdrawnAt: LocalDateTime,
	) : this() {
		this.memberId = memberId
		this.originalEmail = originalEmail
		this.reason = reason
		this.withdrawnAt = withdrawnAt
	}
}
